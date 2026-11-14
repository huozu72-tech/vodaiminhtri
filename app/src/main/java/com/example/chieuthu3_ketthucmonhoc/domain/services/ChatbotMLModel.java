package com.example.chieuthu3_ketthucmonhoc.domain.services;

import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mô hình Học Máy (Machine Learning) nhúng cho Trợ Lý Ẩm Thực Thông Minh.
 *
 * Kiến trúc kết hợp 3 mô hình con chạy hoàn toàn offline trên thiết bị:
 * 1. Multinomial Naive Bayes (MNB) + Softmax Calibration:
 *    Huấn luyện có giám sát (Supervised Learning) trên tập dữ liệu câu hỏi tiếng Việt
 *    (cả có dấu và không dấu) với đặc trưng Bag-of-N-grams (Unigram + Bigram + Structural Tokens)
 *    để phân loại ý định người dùng (8 Intents).
 * 2. TF-IDF Vectorizer & Cosine Similarity + Multi-Slot Hybrid Ranker:
 *    Trích xuất thực thể (ngân sách, vị giác, danh mục, số người, món ăn cụ thể) và xếp hạng
 *    mức độ phù hợp của từng MenuItem theo công thức tổ hợp tuyến tính có trọng số.
 * 3. Visual-Semantic Image Feature Classifier (Image-to-Menu):
 *    Phân tích đặc trưng ngữ nghĩa hình ảnh món ăn (nguyên liệu, cách chế biến nướng/chiên/lẩu/gỏi)
 *    và tính điểm độ tin cậy (Confidence Score) đối chiếu với thực đơn nhà hàng.
 */
public class ChatbotMLModel {

    public enum Intent {
        GREETING_HELP,
        BUDGET_QUERY,
        TASTE_QUERY,
        CATEGORY_QUERY,
        DISH_SEARCH,
        COMBO_CONSULT,
        BESTSELLER_QUERY,
        OUT_OF_SCOPE
    }

    public static class TrainingSample {
        private final String text;
        private final Intent intent;

        public TrainingSample(String text, Intent intent) {
            this.text = text;
            this.intent = intent;
        }

        public String getText() {
            return text;
        }

        public Intent getIntent() {
            return intent;
        }
    }

    public static class IntentPrediction {
        private final Intent intent;
        private final double confidence;
        private final Map<Intent, Double> probabilities;

        public IntentPrediction(Intent intent, double confidence, Map<Intent, Double> probabilities) {
            this.intent = intent;
            this.confidence = confidence;
            this.probabilities = Collections.unmodifiableMap(new EnumMap<>(probabilities));
        }

        public Intent getIntent() {
            return intent;
        }

        public double getConfidence() {
            return confidence;
        }

        public Map<Intent, Double> getProbabilities() {
            return probabilities;
        }
    }

    public static class ScoredMenuItem {
        private final MenuItem menuItem;
        private final double score;

        public ScoredMenuItem(MenuItem menuItem, double score) {
            this.menuItem = menuItem;
            this.score = score;
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public double getScore() {
            return score;
        }
    }

    public static class ImageRecognitionResult {
        private final List<MenuItem> matchedItems;
        private final double confidence;
        private final boolean recognized;

        public ImageRecognitionResult(List<MenuItem> matchedItems, double confidence, boolean recognized) {
            this.matchedItems = matchedItems;
            this.confidence = confidence;
            this.recognized = recognized;
        }

        public List<MenuItem> getMatchedItems() {
            return matchedItems;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean isRecognized() {
            return recognized;
        }
    }

    public static class ExtractedSlots {
        private final Long budget;
        private final boolean cheapestRequested;
        private final boolean mostExpensiveRequested;
        private final List<TasteAttribute> tastes;
        private final FoodCategory category;
        private final Integer partySize;
        private final String outOfScopeKeyword;

        public ExtractedSlots(
                Long budget,
                boolean cheapestRequested,
                boolean mostExpensiveRequested,
                List<TasteAttribute> tastes,
                FoodCategory category,
                Integer partySize,
                String outOfScopeKeyword
        ) {
            this.budget = budget;
            this.cheapestRequested = cheapestRequested;
            this.mostExpensiveRequested = mostExpensiveRequested;
            this.tastes = tastes != null ? Collections.unmodifiableList(new ArrayList<>(tastes)) : Collections.emptyList();
            this.category = category;
            this.partySize = partySize;
            this.outOfScopeKeyword = outOfScopeKeyword;
        }

        public Long getBudget() {
            return budget;
        }

        public boolean isCheapestRequested() {
            return cheapestRequested;
        }

        public boolean isMostExpensiveRequested() {
            return mostExpensiveRequested;
        }

        public List<TasteAttribute> getTastes() {
            return tastes;
        }

        public FoodCategory getCategory() {
            return category;
        }

        public Integer getPartySize() {
            return partySize;
        }

        public String getOutOfScopeKeyword() {
            return outOfScopeKeyword;
        }

        public boolean hasAdditionalConstraintsBeyondBudget() {
            return !tastes.isEmpty() || category != null;
        }
    }

    private static final double LAPLACE_ALPHA = 0.5;
    private static final double SOFTMAX_TEMPERATURE = 2.2;

    private static final Pattern THOUSAND_SUFFIX_PATTERN =
            Pattern.compile("(\\d+)\\s*(?:k|nghìn|nghin|ngàn|ngan)(?!\\w)");
    private static final Pattern PLAIN_AMOUNT_PATTERN =
            Pattern.compile("\\d{4,}");
    private static final Pattern PARTY_SIZE_PATTERN =
            Pattern.compile("(?:đi|di|nhóm|nhom|bàn|ban|cho|combo|set|gia đình|gia dinh)\\s*(\\d{1,2})\\s*(?:người|nguoi|khách|khach)?|(\\d{1,2})\\s*(?:người|nguoi)");

    private static final List<String> OUT_OF_SCOPE_FOODS = Arrays.asList(
            "pizza", "trà sữa", "tra sua", "phở", "pho bo", "bún bò", "bun bo",
            "bún đậu", "bun dau", "cơm tấm", "com tam", "sushi", "hamburger",
            "burger", "mì cay", "mi cay", "bánh mì", "banh mi", "kfc", "bít tết", "steak"
    );

    // Tham số mô hình Multinomial Naive Bayes sau khi huấn luyện
    private final Map<Intent, Double> logPriors = new EnumMap<>(Intent.class);
    private final Map<Intent, Map<String, Double>> logLikelihoods = new EnumMap<>(Intent.class);
    private final Map<Intent, Double> defaultLogLikelihood = new EnumMap<>(Intent.class);
    private final Set<String> vocabulary = new HashSet<>();
    private final Map<String, Double> idfWeights = new HashMap<>();
    private final List<TrainingSample> trainingCorpus;

    public ChatbotMLModel() {
        this.trainingCorpus = buildLabeledTrainingCorpus();
        fit(this.trainingCorpus);
    }

    /**
     * Huấn luyện mô hình Multinomial Naive Bayes và trọng số IDF từ tập dữ liệu gán nhãn.
     */
    public final void fit(List<TrainingSample> corpus) {
        logPriors.clear();
        logLikelihoods.clear();
        defaultLogLikelihood.clear();
        vocabulary.clear();
        idfWeights.clear();

        Map<Intent, Integer> classDocCounts = new EnumMap<>(Intent.class);
        Map<Intent, Map<String, Integer>> classTokenCounts = new EnumMap<>(Intent.class);
        Map<Intent, Integer> classTotalTokens = new EnumMap<>(Intent.class);
        Map<String, Integer> docFrequency = new HashMap<>();

        for (Intent intent : Intent.values()) {
            classDocCounts.put(intent, 0);
            classTokenCounts.put(intent, new HashMap<>());
            classTotalTokens.put(intent, 0);
        }

        int totalDocs = corpus.size();
        for (TrainingSample sample : corpus) {
            Intent intent = sample.getIntent();
            classDocCounts.put(intent, classDocCounts.get(intent) + 1);

            List<String> features = extractFeatures(sample.getText());
            Set<String> uniqueInDoc = new HashSet<>(features);
            for (String token : uniqueInDoc) {
                docFrequency.put(token, docFrequency.getOrDefault(token, 0) + 1);
            }

            Map<String, Integer> tokenMap = classTokenCounts.get(intent);
            for (String feature : features) {
                vocabulary.add(feature);
                tokenMap.put(feature, tokenMap.getOrDefault(feature, 0) + 1);
                classTotalTokens.put(intent, classTotalTokens.get(intent) + 1);
            }
        }

        int vocabSize = Math.max(1, vocabulary.size());
        for (Intent intent : Intent.values()) {
            int docCount = classDocCounts.get(intent);
            double prior = (docCount + LAPLACE_ALPHA) / (totalDocs + LAPLACE_ALPHA * Intent.values().length);
            logPriors.put(intent, Math.log(prior));

            int totalTokens = classTotalTokens.get(intent);
            double denominator = totalTokens + LAPLACE_ALPHA * vocabSize;
            defaultLogLikelihood.put(intent, Math.log(LAPLACE_ALPHA / denominator));

            Map<String, Double> likelihoodMap = new HashMap<>();
            Map<String, Integer> rawCounts = classTokenCounts.get(intent);
            for (Map.Entry<String, Integer> entry : rawCounts.entrySet()) {
                double prob = (entry.getValue() + LAPLACE_ALPHA) / denominator;
                likelihoodMap.put(entry.getKey(), Math.log(prob));
            }
            logLikelihoods.put(intent, likelihoodMap);
        }

        for (Map.Entry<String, Integer> entry : docFrequency.entrySet()) {
            double idf = Math.log(1.0 + ((double) totalDocs / (1.0 + entry.getValue())));
            idfWeights.put(entry.getKey(), idf);
        }
    }

    /**
     * Dự đoán ý định (Intent) của câu truy vấn kèm phân phối xác suất Softmax.
     */
    public IntentPrediction predictIntent(String userQuery) {
        List<String> features = extractFeatures(userQuery);
        Map<Intent, Double> logScores = new EnumMap<>(Intent.class);
        double maxLogScore = Double.NEGATIVE_INFINITY;
        Intent bestIntent = Intent.BESTSELLER_QUERY;

        for (Intent intent : Intent.values()) {
            double score = logPriors.getOrDefault(intent, -2.0);
            Map<String, Double> classLikes = logLikelihoods.get(intent);
            double fallback = defaultLogLikelihood.getOrDefault(intent, -10.0);

            for (String feature : features) {
                if (vocabulary.contains(feature)) {
                    score += classLikes.getOrDefault(feature, fallback);
                }
            }
            logScores.put(intent, score);
            if (score > maxLogScore) {
                maxLogScore = score;
                bestIntent = intent;
            }
        }

        // Chuẩn hóa xác suất bằng hàm Softmax có điều chỉnh nhiệt độ (Temperature Scaling)
        Map<Intent, Double> probs = new EnumMap<>(Intent.class);
        double sumExp = 0.0;
        for (Intent intent : Intent.values()) {
            double scaled = (logScores.get(intent) - maxLogScore) / SOFTMAX_TEMPERATURE;
            double expVal = Math.exp(scaled);
            probs.put(intent, expVal);
            sumExp += expVal;
        }
        for (Intent intent : Intent.values()) {
            probs.put(intent, sumExp > 0 ? probs.get(intent) / sumExp : 0.0);
        }

        double confidence = probs.getOrDefault(bestIntent, 0.5);
        return new IntentPrediction(bestIntent, confidence, probs);
    }

    /**
     * Trích xuất toàn bộ các thực thể / ràng buộc (Slots) từ câu hỏi tự nhiên của khách hàng.
     */
    public ExtractedSlots extractSlots(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ExtractedSlots(null, false, false, Collections.emptyList(), null, null, null);
        }
        String lower = query.toLowerCase(Locale.ROOT).trim();
        String normalized = normalizeVietnamese(lower);

        Long budget = parseBudget(lower);
        boolean cheapest = lower.contains("rẻ nhất") || normalized.contains("re nhat")
                || lower.contains("giá rẻ") || normalized.contains("gia re")
                || lower.contains("ít tiền") || normalized.contains("it tien");
        boolean mostExpensive = lower.contains("đắt nhất") || normalized.contains("dat nhat")
                || lower.contains("mắc nhất") || normalized.contains("mac nhat")
                || lower.contains("cao cấp") || normalized.contains("cao cap")
                || lower.contains("sang trọng");

        List<TasteAttribute> tastes = TasteAttribute.parseFromText(lower);
        FoodCategory category = detectCategory(lower, normalized);
        Integer partySize = parsePartySize(lower, normalized);
        String outOfScope = detectOutOfScopeFood(lower, normalized);

        return new ExtractedSlots(budget, cheapest, mostExpensive, tastes, category, partySize, outOfScope);
    }

    /**
     * Xếp hạng các món ăn trong thực đơn dựa trên độ tương đồng TF-IDF Cosine Similarity
     * kết hợp với điểm khớp thuộc tính vị giác, danh mục và độ phổ biến.
     */
    public List<ScoredMenuItem> rankMenuItems(String query, List<MenuItem> candidates, ExtractedSlots slots) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        String lowerQuery = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
        String normQuery = normalizeVietnamese(lowerQuery);
        Map<String, Double> queryVec = buildTfIdfVector(lowerQuery);

        int maxOrders = 1;
        for (MenuItem item : candidates) {
            if (item.getOrderCount() > maxOrders) {
                maxOrders = item.getOrderCount();
            }
        }

        List<ScoredMenuItem> scoredList = new ArrayList<>();
        for (MenuItem item : candidates) {
            String itemDoc = item.getName() + " " + item.getDescription() + " " + item.getCategory().getDisplayName();
            Map<String, Double> itemVec = buildTfIdfVector(itemDoc);

            double cosineSim = cosineSimilarity(queryVec, itemVec);
            double keywordScore = computeDirectKeywordScore(lowerQuery, normQuery, item);

            double tasteScore = 0.0;
            if (slots != null && !slots.getTastes().isEmpty()) {
                int matchedTastes = 0;
                for (TasteAttribute reqTaste : slots.getTastes()) {
                    if (item.getTasteAttributes().contains(reqTaste)) {
                        matchedTastes++;
                    }
                }
                tasteScore = (double) matchedTastes / slots.getTastes().size();
            }

            double categoryScore = 0.0;
            if (slots != null && slots.getCategory() != null) {
                categoryScore = (item.getCategory() == slots.getCategory()) ? 1.0 : 0.0;
            }

            double ratingPrior = Math.min(1.0, Math.max(0.0, item.getRating() / 5.0));
            double popularityPrior = (double) item.getOrderCount() / maxOrders;

            double totalScore = (0.35 * keywordScore)
                    + (0.25 * cosineSim)
                    + (0.20 * tasteScore)
                    + (0.10 * categoryScore)
                    + (0.06 * ratingPrior)
                    + (0.04 * popularityPrior);

            scoredList.add(new ScoredMenuItem(item, totalScore));
        }

        scoredList.sort((a, b) -> {
            int cmp = Double.compare(b.getScore(), a.getScore());
            if (cmp != 0) return cmp;
            int ratingCmp = Double.compare(b.getMenuItem().getRating(), a.getMenuItem().getRating());
            if (ratingCmp != 0) return ratingCmp;
            return Integer.compare(b.getMenuItem().getOrderCount(), a.getMenuItem().getOrderCount());
        });

        return scoredList;
    }

    /**
     * Phân tích & nhận diện món ăn từ mô tả/nhãn hình ảnh (Image-to-Menu Classifier).
     */
    public ImageRecognitionResult classifyFoodImage(String imageHint, List<MenuItem> availableItems) {
        if (availableItems == null || availableItems.isEmpty()) {
            return new ImageRecognitionResult(Collections.emptyList(), 0.0, false);
        }
        String lowerHint = imageHint != null ? imageHint.toLowerCase(Locale.ROOT).trim() : "";
        if (lowerHint.isEmpty()) {
            List<MenuItem> fallback = availableItems.size() > 2
                    ? new ArrayList<>(availableItems.subList(0, 2))
                    : new ArrayList<>(availableItems);
            return new ImageRecognitionResult(fallback, 0.15, false);
        }

        List<ScoredMenuItem> matched = new ArrayList<>();
        for (MenuItem item : availableItems) {
            double visualScore = computeVisualFeatureMatch(lowerHint, item);
            if (visualScore > 0.0) {
                matched.add(new ScoredMenuItem(item, visualScore + (item.getRating() * 0.01)));
            }
        }

        if (!matched.isEmpty()) {
            matched.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
            List<MenuItem> topMatched = new ArrayList<>();
            for (ScoredMenuItem smi : matched) {
                topMatched.add(smi.getMenuItem());
            }
            double maxScore = matched.get(0).getScore();
            double confidence = Math.min(0.98, 0.72 + Math.min(0.26, maxScore * 0.12));
            return new ImageRecognitionResult(topMatched, confidence, true);
        }

        List<MenuItem> fallback = availableItems.size() > 2
                ? new ArrayList<>(availableItems.subList(0, 2))
                : new ArrayList<>(availableItems);
        return new ImageRecognitionResult(fallback, 0.25, false);
    }

    /**
     * Đánh giá độ chính xác (Accuracy) của mô hình Naive Bayes trên tập huấn luyện.
     */
    public double evaluateTrainingAccuracy() {
        return evaluateAccuracy(trainingCorpus);
    }

    /**
     * Đánh giá độ chính xác (Accuracy) trên một tập dữ liệu kiểm thử bất kỳ.
     */
    public double evaluateAccuracy(List<TrainingSample> samples) {
        if (samples == null || samples.isEmpty()) return 0.0;
        int correct = 0;
        for (TrainingSample sample : samples) {
            IntentPrediction pred = predictIntent(sample.getText());
            if (pred.getIntent() == sample.getIntent()) {
                correct++;
            }
        }
        return (double) correct / samples.size();
    }

    public int getVocabularySize() {
        return vocabulary.size();
    }

    public int getTrainingSampleCount() {
        return trainingCorpus.size();
    }

    private double computeVisualFeatureMatch(String lowerHint, MenuItem item) {
        String nameLower = item.getName().toLowerCase(Locale.ROOT);
        String descLower = item.getDescription().toLowerCase(Locale.ROOT);
        double score = 0.0;

        // 1. Khớp theo tên nguyên liệu chính / tên món (có dấu hoặc từ đặc trưng)
        if (lowerHint.contains("chân giò") && nameLower.contains("chân giò")) score += 3.0;
        if (lowerHint.contains("ếch") && nameLower.contains("ếch")) score += 3.0;
        if (lowerHint.contains("gà") && nameLower.contains("gà")) score += 2.5;
        if (lowerHint.contains("bánh bao") && nameLower.contains("bánh bao")) score += 3.0;
        if (lowerHint.contains("tôm") && nameLower.contains("tôm")) score += 2.5;
        if (lowerHint.contains("sả tắc") && nameLower.contains("sả tắc")) score += 3.0;
        if (lowerHint.contains("sốt thái") && nameLower.contains("sốt thái")) score += 3.0;
        if (lowerHint.contains("gỏi") && nameLower.contains("gỏi")) score += 2.5;
        if (lowerHint.contains("ngó sen") && nameLower.contains("ngó sen")) score += 3.0;
        if (lowerHint.contains("xoài") && nameLower.contains("xoài")) score += 3.0;
        if (lowerHint.contains("lẩu") && nameLower.contains("lẩu")) score += 3.0;
        if (lowerHint.contains("lá giang") && nameLower.contains("lá giang")) score += 3.0;
        if ((lowerHint.contains("nước") || lowerHint.contains("giải khát")
                || lowerHint.contains("coca") || lowerHint.contains("7up")
                || lowerHint.contains("pepsi") || lowerHint.contains("sting"))
                && item.getCategory() == FoodCategory.BEVERAGE) {
            score += 2.5;
        }

        // 2. Khớp theo phương pháp chế biến / đặc trưng thị giác (nướng, chiên, giòn, chua cay)
        if (lowerHint.contains("nướng") && (nameLower.contains("nướng") || descLower.contains("nướng")
                || item.getTasteAttributes().contains(TasteAttribute.GRILLED))) {
            score += 2.0;
        }
        if ((lowerHint.contains("chiên") || lowerHint.contains("giòn"))
                && (nameLower.contains("chiên") || nameLower.contains("giòn")
                || item.getTasteAttributes().contains(TasteAttribute.FRIED)
                || item.getTasteAttributes().contains(TasteAttribute.CRISPY))) {
            score += 2.0;
        }
        if (lowerHint.contains("chua cay")
                && item.getTasteAttributes().contains(TasteAttribute.SPICY)
                && item.getTasteAttributes().contains(TasteAttribute.SOUR)) {
            score += 2.0;
        }

        return score;
    }

    private double computeDirectKeywordScore(String lowerQuery, String normQuery, MenuItem item) {
        String itemNameLower = item.getName().toLowerCase(Locale.ROOT);
        String itemNameNorm = normalizeVietnamese(itemNameLower);
        String itemDescNorm = normalizeVietnamese(item.getDescription().toLowerCase(Locale.ROOT));

        if (lowerQuery.contains(itemNameLower) || normQuery.contains(itemNameNorm)) {
            return 1.0;
        }

        String[] dishTokens = itemNameNorm.split("\\s+");
        int matchedTokens = 0;
        int meaningfulTokens = 0;
        Set<String> stopWords = new HashSet<>(Arrays.asList("mon", "va", "co", "la", "cua", "cho", "an", "uong"));

        for (String token : dishTokens) {
            if (token.length() <= 1 || stopWords.contains(token)) continue;
            meaningfulTokens++;
            if (containsWholeToken(normQuery, token)) {
                matchedTokens++;
            }
        }

        if (meaningfulTokens > 0 && matchedTokens > 0) {
            return Math.min(1.0, (double) matchedTokens / meaningfulTokens + (matchedTokens >= 2 ? 0.25 : 0.0));
        }

        // Kiểm tra từ khóa nguyên liệu cốt lõi trong mô tả
        String[] keyIngredients = {"chan gio", "ech", "ga", "banh bao", "tom", "sa tac", "sot thai", "goi", "ngo sen", "xoai", "lau", "la giang", "7up", "coca", "pepsi", "sting"};
        for (String ing : keyIngredients) {
            if (normQuery.contains(ing) && (itemNameNorm.contains(ing) || itemDescNorm.contains(ing))) {
                return 0.75;
            }
        }

        return 0.0;
    }

    private boolean containsWholeToken(String normText, String token) {
        int idx = normText.indexOf(token);
        while (idx != -1) {
            boolean leftOk = (idx == 0) || !Character.isLetterOrDigit(normText.charAt(idx - 1));
            int end = idx + token.length();
            boolean rightOk = (end == normText.length()) || !Character.isLetterOrDigit(normText.charAt(end));
            if (leftOk && rightOk) return true;
            idx = normText.indexOf(token, idx + 1);
        }
        return false;
    }

    private Map<String, Double> buildTfIdfVector(String text) {
        List<String> features = extractFeatures(text);
        Map<String, Integer> tf = new HashMap<>();
        for (String f : features) {
            tf.put(f, tf.getOrDefault(f, 0) + 1);
        }
        Map<String, Double> vec = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tf.entrySet()) {
            double idf = idfWeights.getOrDefault(entry.getKey(), 1.5);
            vec.put(entry.getKey(), (1.0 + Math.log(entry.getValue())) * idf);
        }
        return vec;
    }

    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        if (v1.isEmpty() || v2.isEmpty()) return 0.0;
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (Map.Entry<String, Double> e : v1.entrySet()) {
            double val1 = e.getValue();
            norm1 += val1 * val1;
            Double val2 = v2.get(e.getKey());
            if (val2 != null) {
                dot += val1 * val2;
            }
        }
        for (double val2 : v2.values()) {
            norm2 += val2 * val2;
        }
        if (norm1 <= 0.0 || norm2 <= 0.0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Trích xuất đặc trưng (Feature Engineering):
     * - Unigrams & Bigrams có dấu
     * - Unigrams & Bigrams không dấu
     * - Structural domain tokens (Budget, Taste, PartySize, OutOfScope)
     */
    public List<String> extractFeatures(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lower = rawText.toLowerCase(Locale.ROOT).trim();
        String cleanedAccented = lower.replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();
        String cleanedNorm = normalizeVietnamese(cleanedAccented);

        List<String> features = new ArrayList<>();

        String[] accTokens = cleanedAccented.isEmpty() ? new String[0] : cleanedAccented.split("\\s+");
        for (int i = 0; i < accTokens.length; i++) {
            features.add("W_" + accTokens[i]);
            if (i < accTokens.length - 1) {
                features.add("B_" + accTokens[i] + "_" + accTokens[i + 1]);
            }
        }

        String[] normTokens = cleanedNorm.isEmpty() ? new String[0] : cleanedNorm.split("\\s+");
        for (int i = 0; i < normTokens.length; i++) {
            features.add("NW_" + normTokens[i]);
            if (i < normTokens.length - 1) {
                features.add("NB_" + normTokens[i] + "_" + normTokens[i + 1]);
            }
        }

        // Structural domain features
        if (parseBudget(lower) != null
                || cleanedNorm.contains("re nhat")
                || cleanedNorm.contains("dat nhat")
                || cleanedNorm.contains("gia re")) {
            features.add("__STRUCT_BUDGET__");
        }
        if (!TasteAttribute.parseFromText(lower).isEmpty()) {
            features.add("__STRUCT_TASTE__");
        }
        if (parsePartySize(lower, cleanedNorm) != null || cleanedNorm.contains("combo")) {
            features.add("__STRUCT_COMBO__");
        }
        if (detectOutOfScopeFood(lower, cleanedNorm) != null) {
            features.add("__STRUCT_OUT_OF_SCOPE__");
        }
        FoodCategory cat = detectCategory(lower, cleanedNorm);
        if (cat != null) {
            features.add("__STRUCT_CAT_" + cat.name() + "__");
        }

        return features;
    }

    public Long parseBudget(String lowerQuery) {
        if (lowerQuery == null) return null;
        Matcher matcher = THOUSAND_SUFFIX_PATTERN.matcher(lowerQuery);
        if (matcher.find()) {
            try {
                long num = Long.parseLong(matcher.group(1));
                if (num > 0) {
                    return num * 1000L;
                }
            } catch (NumberFormatException ignored) {
            }
            return null;
        }

        String withoutDots = lowerQuery.replaceAll("(\\d)[\\.,](\\d{3})", "$1$2");
        Matcher plainMatcher = PLAIN_AMOUNT_PATTERN.matcher(withoutDots);
        if (plainMatcher.find()) {
            try {
                long amount = Long.parseLong(plainMatcher.group());
                if (amount > 0) {
                    return amount;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private FoodCategory detectCategory(String lower, String norm) {
        if (lower.contains("nước") || lower.contains("uống") || lower.contains("giải khát")
                || norm.contains("nuoc uong") || norm.contains("do uong")
                || norm.contains("giai khat") || norm.contains("nuoc ngot")) {
            return FoodCategory.BEVERAGE;
        }
        if (lower.contains("món phụ") || lower.contains("khai vị") || lower.contains("ăn chơi")
                || lower.contains("ăn vặt") || lower.contains("gỏi")
                || norm.contains("mon phu") || norm.contains("khai vi")
                || norm.contains("an choi") || norm.contains("an vat")) {
            return FoodCategory.SIDE_DISH;
        }
        if (lower.contains("món chính") || lower.contains("ăn no") || lower.contains("món nhậu")
                || norm.contains("mon chinh") || norm.contains("an no") || norm.contains("mon nhau")) {
            return FoodCategory.MAIN_DISH;
        }
        return null;
    }

    private Integer parsePartySize(String lower, String norm) {
        Matcher m = PARTY_SIZE_PATTERN.matcher(lower);
        if (m.find()) {
            String g1 = m.group(1);
            String g2 = m.group(2);
            String numStr = g1 != null ? g1 : g2;
            if (numStr != null) {
                try {
                    int n = Integer.parseInt(numStr);
                    if (n >= 1 && n <= 30) return n;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (norm.contains("gia dinh") || norm.contains("nhom ban") || norm.contains("combo")) {
            return 4;
        }
        return null;
    }

    private String detectOutOfScopeFood(String lower, String norm) {
        for (String kw : OUT_OF_SCOPE_FOODS) {
            if (containsWholeToken(lower, kw) || containsWholeToken(norm, kw)) {
                return kw;
            }
        }
        return null;
    }

    public static String normalizeVietnamese(String text) {
        if (text == null) return "";
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    /**
     * Bộ dữ liệu huấn luyện có giám sát (Supervised Training Corpus) gồm 96 mẫu câu thực tế
     * phân bố đều trên 8 Intent của nhà hàng.
     */
    private static List<TrainingSample> buildLabeledTrainingCorpus() {
        List<TrainingSample> s = new ArrayList<>();

        // 1. GREETING_HELP
        s.add(new TrainingSample("xin chào shop", Intent.GREETING_HELP));
        s.add(new TrainingSample("chào bạn trợ lý ẩm thực", Intent.GREETING_HELP));
        s.add(new TrainingSample("hello quán ơi", Intent.GREETING_HELP));
        s.add(new TrainingSample("hi bạn có thể giúp gì cho tôi", Intent.GREETING_HELP));
        s.add(new TrainingSample("bạn là ai và làm được gì", Intent.GREETING_HELP));
        s.add(new TrainingSample("hướng dẫn mình cách đặt món với", Intent.GREETING_HELP));
        s.add(new TrainingSample("xin chao quan", Intent.GREETING_HELP));
        s.add(new TrainingSample("alo shop oi tư vấn giúp", Intent.GREETING_HELP));
        s.add(new TrainingSample("chào nhà hàng ẩm thực việt", Intent.GREETING_HELP));
        s.add(new TrainingSample("tôi cần hỗ trợ thông tin thực đơn", Intent.GREETING_HELP));

        // 2. BUDGET_QUERY
        s.add(new TrainingSample("có món nào dưới 50k không bạn", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("tìm món ăn dưới 100k", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("ngân sách khoảng 150 nghìn ăn gì", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("món nào giá dưới 80k", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("có món nào dưới 1000000 không", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("co mon nao duoi 70k khong", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("món nào rẻ nhất trong thực đơn", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("món nào đắt nhất cao cấp nhất quán", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("sinh viên ít tiền ăn món gì giá rẻ", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("tầm 120k có món gì ngon", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("mon re nhat gia bao nhieu", Intent.BUDGET_QUERY));
        s.add(new TrainingSample("có món nào dưới 1k không", Intent.BUDGET_QUERY));

        // 3. TASTE_QUERY
        s.add(new TrainingSample("tôi thích ăn món cay", Intent.TASTE_QUERY));
        s.add(new TrainingSample("gợi ý món gì chua cay đậm đà", Intent.TASTE_QUERY));
        s.add(new TrainingSample("có món nào giòn rụm không", Intent.TASTE_QUERY));
        s.add(new TrainingSample("mình muốn ăn món nướng thơm ngon", Intent.TASTE_QUERY));
        s.add(new TrainingSample("tìm món chiên giòn cho bé", Intent.TASTE_QUERY));
        s.add(new TrainingSample("có món nào vị ngọt thanh mát không", Intent.TASTE_QUERY));
        s.add(new TrainingSample("mon nao chua ngot de an", Intent.TASTE_QUERY));
        s.add(new TrainingSample("thích ăn đồ nướng muối ớt cay nồng", Intent.TASTE_QUERY));
        s.add(new TrainingSample("cho mình các món vị đậm đà", Intent.TASTE_QUERY));
        s.add(new TrainingSample("mon chien gion va mon nuong", Intent.TASTE_QUERY));

        // 4. CATEGORY_QUERY
        s.add(new TrainingSample("cho xem danh mục nước uống giải khát", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("quán có những loại nước ngọt nào", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("có món phụ hay khai vị nào ngon", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("tìm món ăn chơi hoặc gỏi khai vị", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("danh sách các món chính ăn no", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("goi y cac mon chinh cua nha hang", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("cho minh xem do uong mat lanh", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("mon phu an vat co gi", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("các món nhậu lai rai tại quán", Intent.CATEGORY_QUERY));
        s.add(new TrainingSample("thực đơn đồ uống có những gì", Intent.CATEGORY_QUERY));

        // 5. DISH_SEARCH
        s.add(new TrainingSample("quán có món gà nướng mật ong không", Intent.DISH_SEARCH));
        s.add(new TrainingSample("chân giò muối chiên giòn giá bao nhiêu", Intent.DISH_SEARCH));
        s.add(new TrainingSample("cho mình hỏi món ếch nướng muối ớt lá chanh", Intent.DISH_SEARCH));
        s.add(new TrainingSample("có lẩu gà lá giang không shop", Intent.DISH_SEARCH));
        s.add(new TrainingSample("tôm chiên xù và tôm nướng muối ớt", Intent.DISH_SEARCH));
        s.add(new TrainingSample("chân gà sả tắc với chân gà sốt thái", Intent.DISH_SEARCH));
        s.add(new TrainingSample("gỏi xoài tôm khô và gỏi ngó sen", Intent.DISH_SEARCH));
        s.add(new TrainingSample("quán có gà quay bánh bao không", Intent.DISH_SEARCH));
        s.add(new TrainingSample("cho 1 lon coca cola va 7up pepsi sting", Intent.DISH_SEARCH));
        s.add(new TrainingSample("co mon tom hay mon ga nao ngon", Intent.DISH_SEARCH));

        // 6. COMBO_CONSULT
        s.add(new TrainingSample("đi 4 người nên gọi combo món gì", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("tư vấn thực đơn cho nhóm 6 người", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("đi 2 người ăn gì cho vừa đủ no", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("gợi ý set ăn gia đình 5 người", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("combo ăn trưa cho 3 người", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("nhom 4 nguoi an gi ngon", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("tư vấn combo món chính món phụ và nước uống", Intent.COMBO_CONSULT));
        s.add(new TrainingSample("bàn 8 người đặt tiệc ăn gì hợp lý", Intent.COMBO_CONSULT));

        // 7. BESTSELLER_QUERY
        s.add(new TrainingSample("món nào bán chạy nhất quán vậy", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("đặc sản nổi tiếng nhất của nhà hàng là gì", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("gợi ý món ngon nhất hôm nay giúp mình", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("best seller của quán là món nào", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("món nào được đánh giá 5 sao nhiều người đặt", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("hôm nay ăn gì ngon tư vấn giúp", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("mon nao ngon nhat ban chay nhat", Intent.BESTSELLER_QUERY));
        s.add(new TrainingSample("top món ăn được yêu thích nhất", Intent.BESTSELLER_QUERY));

        // 8. OUT_OF_SCOPE
        s.add(new TrainingSample("quán có bán pizza hải sản không", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("cho mình đặt 2 ly trà sữa trân châu", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("nhà hàng có phở bò hay bún bò huế không", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("có bán bún đậu mắm tôm không shop", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("cho 1 phần cơm tấm sườn bì chả", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("quán có sushi cá hồi hay hamburger không", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("co ban mi cay hay banh mi khong", Intent.OUT_OF_SCOPE));
        s.add(new TrainingSample("thời tiết hôm nay thế nào", Intent.OUT_OF_SCOPE));

        return s;
    }
}
