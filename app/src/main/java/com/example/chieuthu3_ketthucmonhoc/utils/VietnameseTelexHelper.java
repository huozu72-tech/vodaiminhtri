package com.example.chieuthu3_ketthucmonhoc.utils;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bộ gõ Tiếng Việt Telex thông minh tích hợp trực tiếp vào EditText.
 * Giúp người dùng gõ tiếng Việt có dấu mượt mà ngay cả trên máy ảo Android (Emulator)
 * với bàn phím cứng PC hoặc bàn phím ảo tiếng Anh mặc định.
 *
 * Hỗ trợ đầy đủ chuẩn Telex:
 * - Chữ cái: aa -> â, aw -> ă, ee -> ê, oo -> ô, ow -> ơ, uw -> ư, dd -> đ, uow / uwo -> ươ
 * - Dấu thanh (gõ ngay sau nguyên âm hoặc cuối từ):
 *   s (sắc), f (huyền), r (hỏi), x (ngã), j (nặng), z (xóa dấu thanh)
 */
public final class VietnameseTelexHelper {

    private static final char[][] VOWEL_TABLE = {
            // 0: a
            {'a', 'á', 'à', 'ả', 'ã', 'ạ'},
            // 1: ă
            {'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ'},
            // 2: â
            {'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'},
            // 3: e
            {'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ'},
            // 4: ê
            {'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ'},
            // 5: i
            {'i', 'í', 'ì', 'ỉ', 'ĩ', 'ị'},
            // 6: o
            {'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ'},
            // 7: ô
            {'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ'},
            // 8: ơ
            {'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ'},
            // 9: u
            {'u', 'ú', 'ù', 'ủ', 'ũ', 'ụ'},
            // 10: ư
            {'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự'},
            // 11: y
            {'y', 'ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ'}
    };

    private static final Map<Character, int[]> CHAR_TO_VOWEL_INFO = new HashMap<>();

    static {
        for (int row = 0; row < VOWEL_TABLE.length; row++) {
            for (int tone = 0; tone < VOWEL_TABLE[row].length; tone++) {
                char lower = VOWEL_TABLE[row][tone];
                char upper = Character.toUpperCase(lower);
                CHAR_TO_VOWEL_INFO.put(lower, new int[]{row, tone, 0});
                CHAR_TO_VOWEL_INFO.put(upper, new int[]{row, tone, 1});
            }
        }
    }

    private VietnameseTelexHelper() {
    }

    /**
     * Gắn bộ gõ Tiếng Việt Telex trực tiếp vào một EditText.
     *
     * @param editText Ô nhập liệu cần hỗ trợ gõ Tiếng Việt có dấu
     * @param enabledFlag Mảng boolean 1 phần tử cho phép bật/tắt nhanh chế độ Telex (null = luôn bật)
     */
    public static void attachToEditText(final EditText editText, final boolean[] enabledFlag) {
        if (editText == null) return;

        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            private int lastStart = 0;
            private int lastCount = 0;
            private int lastBefore = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.lastStart = start;
                this.lastBefore = before;
                this.lastCount = count;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdating) return;
                if (enabledFlag != null && enabledFlag.length > 0 && !enabledFlag[0]) return;

                // Chỉ kích hoạt tổ hợp dấu khi người dùng vừa gõ thêm 1 ký tự mới
                if (lastCount != 1 || lastBefore != 0) return;

                int cursor = lastStart + 1;
                if (cursor <= 0 || cursor > editable.length()) return;

                char typedChar = editable.charAt(cursor - 1);
                if (!Character.isLetter(typedChar)) return;

                // Tìm ranh giới từ hiện tại tính đến vị trí con trỏ
                int wordStart = cursor - 1;
                while (wordStart > 0 && Character.isLetter(editable.charAt(wordStart - 1))) {
                    wordStart--;
                }

                String currentWord = editable.subSequence(wordStart, cursor).toString();
                if (currentWord.length() < 2) return;

                String wordBefore = currentWord.substring(0, currentWord.length() - 1);
                String transformed = applyTelexKeystroke(wordBefore, typedChar);

                if (!transformed.equals(currentWord)) {
                    isUpdating = true;
                    try {
                        editable.replace(wordStart, cursor, transformed);
                        int newCursor = wordStart + transformed.length();
                        if (newCursor <= editable.length()) {
                            Selection.setSelection(editable, newCursor);
                        }
                    } finally {
                        isUpdating = false;
                    }
                }
            }
        });
    }

    /**
     * Chuyển đổi một câu hoặc cụm từ gõ theo kiểu Telex sang tiếng Việt có dấu.
     */
    public static String convertTelexSentence(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetter(c)) {
                if (currentWord.length() == 0) {
                    currentWord.append(c);
                } else {
                    String updated = applyTelexKeystroke(currentWord.toString(), c);
                    currentWord.setLength(0);
                    currentWord.append(updated);
                }
            } else {
                if (currentWord.length() > 0) {
                    result.append(currentWord);
                    currentWord.setLength(0);
                }
                result.append(c);
            }
        }
        if (currentWord.length() > 0) {
            result.append(currentWord);
        }
        return result.toString();
    }

    /**
     * Áp dụng 1 phím gõ Telex (`typedChar`) vào từ đang gõ (`wordBefore`).
     */
    public static String applyTelexKeystroke(String wordBefore, char typedChar) {
        if (wordBefore == null || wordBefore.isEmpty()) {
            return String.valueOf(typedChar);
        }

        char lowerKey = Character.toLowerCase(typedChar);

        // 1. Xử lý 'd' + 'd' -> 'đ' (hoặc ở đầu từ như ddi + d -> đi)
        if (lowerKey == 'd') {
            char lastChar = wordBefore.charAt(wordBefore.length() - 1);
            if (lastChar == 'd') {
                return wordBefore.substring(0, wordBefore.length() - 1) + 'đ';
            } else if (lastChar == 'D') {
                return wordBefore.substring(0, wordBefore.length() - 1) + 'Đ';
            } else if (lastChar == 'đ') {
                // Gõ 'd' lần thứ 3 -> hoàn tác về 'dd'
                return wordBefore.substring(0, wordBefore.length() - 1) + "dd";
            } else if (lastChar == 'Đ') {
                return wordBefore.substring(0, wordBefore.length() - 1) + "DD";
            }
            // Trường hợp gõ 'd' ở cuối từ để biến 'd' đầu từ thành 'đ' (VD: "di" + "d" -> "đi")
            char firstChar = wordBefore.charAt(0);
            if ((firstChar == 'd' || firstChar == 'D') && hasVowel(wordBefore)) {
                char replacement = (firstChar == 'D') ? 'Đ' : 'đ';
                return replacement + wordBefore.substring(1);
            }
        }

        // 2. Xử lý các phím dấu mũ / trăng / móc: a, e, o, w
        if (lowerKey == 'a' || lowerKey == 'e' || lowerKey == 'o' || lowerKey == 'w') {
            String modified = tryApplyVowelModifier(wordBefore, typedChar);
            if (modified != null) {
                return rebalanceTone(modified);
            }
        }

        // 3. Xử lý các phím dấu thanh: s (1), f (2), r (3), x (4), j (5), z (0)
        int targetTone = getToneFromKey(lowerKey);
        if (targetTone >= 0 && hasVowel(wordBefore)) {
            String toned = tryApplyTone(wordBefore, targetTone, typedChar);
            if (toned != null) {
                return toned;
            }
        }

        // 4. Khi người dùng gõ thêm phụ âm cuối hoặc nguyên âm nối tiếp sau khi đã có dấu thanh
        // (VD: "mó" + "n" -> "món", "hó" + "a" -> "hóa", "hoá" + "n" -> "hoán")
        String appended = wordBefore + typedChar;
        if (hasVowel(wordBefore)) {
            return rebalanceTone(appended);
        }

        return appended;
    }

    private static int getToneFromKey(char lowerKey) {
        switch (lowerKey) {
            case 's':
                return 1; // Sắc
            case 'f':
                return 2; // Huyền
            case 'r':
                return 3; // Hỏi
            case 'x':
                return 4; // Ngã
            case 'j':
                return 5; // Nặng
            case 'z':
                return 0; // Xóa dấu
            default:
                return -1;
        }
    }

    private static String tryApplyVowelModifier(String wordBefore, char typedChar) {
        char lowerKey = Character.toLowerCase(typedChar);
        char[] chars = wordBefore.toCharArray();

        // Chuyển đổi 'aa' -> 'â', 'ee' -> 'ê', 'oo' -> 'ô'
        if (lowerKey == 'a' || lowerKey == 'e' || lowerKey == 'o') {
            int targetBaseRow = (lowerKey == 'a') ? 0 : (lowerKey == 'e' ? 3 : 6);
            int circumflexRow = (lowerKey == 'a') ? 2 : (lowerKey == 'e' ? 4 : 7);

            for (int i = chars.length - 1; i >= 0; i--) {
                int[] info = CHAR_TO_VOWEL_INFO.get(chars[i]);
                if (info == null) continue;
                if (info[0] == targetBaseRow) {
                    chars[i] = buildVowelChar(circumflexRow, info[1], info[2] == 1);
                    return new String(chars);
                } else if (info[0] == circumflexRow && i == chars.length - 1) {
                    // Gõ lặp lần 3 (VD: aaa -> aa) để hoàn tác dấu mũ
                    chars[i] = buildVowelChar(targetBaseRow, info[1], info[2] == 1);
                    return new String(chars) + typedChar;
                }
            }
            return null;
        }

        // Chuyển đổi 'w':
        // - uo / ưo / uơ + w -> ươ (VD: nuong + w -> nương, duoi + w -> dươi, nuo + w -> nươ)
        // - a + w -> ă
        // - o + w -> ơ
        // - u + w -> ư
        if (lowerKey == 'w') {
            // Kiểm tra cụm uo / ưo / uơ trước
            for (int i = 0; i < chars.length - 1; i++) {
                int[] info1 = CHAR_TO_VOWEL_INFO.get(chars[i]);
                int[] info2 = CHAR_TO_VOWEL_INFO.get(chars[i + 1]);
                if (info1 != null && info2 != null) {
                    boolean firstIsU = (info1[0] == 9 || info1[0] == 10);
                    boolean secondIsO = (info2[0] == 6 || info2[0] == 8);
                    // Tránh biến "qu" + "o" thành "qươ"
                    boolean isQu = (i > 0 && (chars[i - 1] == 'q' || chars[i - 1] == 'Q'));
                    if (!isQu && firstIsU && secondIsO && !(info1[0] == 10 && info2[0] == 8)) {
                        int existingTone = Math.max(info1[1], info2[1]);
                        chars[i] = buildVowelChar(10, 0, info1[2] == 1);
                        chars[i + 1] = buildVowelChar(8, existingTone, info2[2] == 1);
                        return new String(chars);
                    }
                }
            }

            // Duyệt từ cuối từ ngược lên để tìm a, o, u
            for (int i = chars.length - 1; i >= 0; i--) {
                int[] info = CHAR_TO_VOWEL_INFO.get(chars[i]);
                if (info == null) continue;
                // Không biến 'u' trong cụm 'qu' thành 'ư' nếu phía sau có nguyên âm khác
                if (info[0] == 9 && i > 0 && (chars[i - 1] == 'q' || chars[i - 1] == 'Q') && i < chars.length - 1) {
                    continue;
                }
                if (info[0] == 0) { // a -> ă
                    chars[i] = buildVowelChar(1, info[1], info[2] == 1);
                    return new String(chars);
                } else if (info[0] == 6) { // o -> ơ
                    chars[i] = buildVowelChar(8, info[1], info[2] == 1);
                    return new String(chars);
                } else if (info[0] == 9) { // u -> ư
                    chars[i] = buildVowelChar(10, info[1], info[2] == 1);
                    return new String(chars);
                } else if ((info[0] == 1 || info[0] == 8 || info[0] == 10) && i == chars.length - 1) {
                    // Hoàn tác nếu gõ ww liên tiếp
                    int baseRow = (info[0] == 1) ? 0 : (info[0] == 8 ? 6 : 9);
                    chars[i] = buildVowelChar(baseRow, info[1], info[2] == 1);
                    return new String(chars) + typedChar;
                }
            }
        }

        return null;
    }

    private static String tryApplyTone(String wordBefore, int targetTone, char rawTypedChar) {
        char[] chars = wordBefore.toCharArray();
        int currentTone = 0;
        int currentTonePos = -1;

        for (int i = 0; i < chars.length; i++) {
            int[] info = CHAR_TO_VOWEL_INFO.get(chars[i]);
            if (info != null && info[1] > 0) {
                currentTone = info[1];
                currentTonePos = i;
                break;
            }
        }

        // Nếu phím xóa dấu 'z' được bấm khi từ chưa có dấu thanh nào -> giữ nguyên chữ 'z'
        if (targetTone == 0 && currentTone == 0) {
            return null;
        }

        // Nếu người dùng bấm lại chính phím dấu vừa đặt -> hoàn tác dấu và thêm ký tự gốc (VD: "món" + "s" -> "mons")
        if (targetTone > 0 && currentTone == targetTone) {
            int[] info = CHAR_TO_VOWEL_INFO.get(chars[currentTonePos]);
            chars[currentTonePos] = buildVowelChar(info[0], 0, info[2] == 1);
            return new String(chars) + rawTypedChar;
        }

        // Xóa dấu thanh cũ trên toàn bộ nguyên âm
        for (int i = 0; i < chars.length; i++) {
            int[] info = CHAR_TO_VOWEL_INFO.get(chars[i]);
            if (info != null && info[1] > 0) {
                chars[i] = buildVowelChar(info[0], 0, info[2] == 1);
            }
        }

        if (targetTone == 0) {
            return new String(chars);
        }

        int toneIndex = findPrimaryToneIndex(chars);
        if (toneIndex < 0) {
            return null;
        }

        int[] targetInfo = CHAR_TO_VOWEL_INFO.get(chars[toneIndex]);
        chars[toneIndex] = buildVowelChar(targetInfo[0], targetTone, targetInfo[2] == 1);
        return new String(chars);
    }

    /**
     * Cân bằng lại vị trí dấu thanh khi người dùng gõ thêm nguyên âm hoặc phụ âm cuối
     * (VD: "hó" + "a" -> "hóa", "hoá" + "n" -> "hoán", "nướ" + "ng" -> "nướng").
     */
    private static String rebalanceTone(String word) {
        char[] chars = word.toCharArray();
        int existingTone = 0;
        for (int i = 0; i < chars.length; i++) {
            int[] info = CHAR_TO_VOWEL_INFO.get(chars[i]);
            if (info != null && info[1] > 0) {
                existingTone = info[1];
                chars[i] = buildVowelChar(info[0], 0, info[2] == 1);
            }
        }

        // Đồng bộ cụm 'ưo' hoặc 'uơ' đứng trước phụ âm/bán nguyên âm thành 'ươ' (VD: dươ + i -> dươi, nươ + ng -> nương)
        for (int i = 0; i < chars.length - 2; i++) {
            int[] info1 = CHAR_TO_VOWEL_INFO.get(chars[i]);
            int[] info2 = CHAR_TO_VOWEL_INFO.get(chars[i + 1]);
            if (info1 != null && info2 != null) {
                boolean isQu = (i > 0 && Character.toLowerCase(chars[i - 1]) == 'q');
                if (!isQu && ((info1[0] == 10 && info2[0] == 6) || (info1[0] == 9 && info2[0] == 8))) {
                    chars[i] = buildVowelChar(10, 0, info1[2] == 1);
                    chars[i + 1] = buildVowelChar(8, 0, info2[2] == 1);
                }
            }
        }

        if (existingTone == 0) {
            return new String(chars);
        }
        int bestIdx = findPrimaryToneIndex(chars);
        if (bestIdx >= 0) {
            int[] info = CHAR_TO_VOWEL_INFO.get(chars[bestIdx]);
            chars[bestIdx] = buildVowelChar(info[0], existingTone, info[2] == 1);
        }
        return new String(chars);
    }

    /**
     * Xác định vị trí nguyên âm chuẩn để đặt dấu thanh trong âm tiết tiếng Việt.
     */
    private static int findPrimaryToneIndex(char[] chars) {
        int n = chars.length;
        int firstVowel = -1;
        int lastVowel = -1;

        for (int i = 0; i < n; i++) {
            if (CHAR_TO_VOWEL_INFO.containsKey(chars[i])) {
                if (firstVowel == -1) {
                    firstVowel = i;
                }
                lastVowel = i;
            }
        }

        if (firstVowel == -1) return -1;

        // Bỏ qua 'u' trong 'qu' và 'i' trong 'gi' nếu ngay sau đó còn nguyên âm khác
        if (firstVowel < lastVowel) {
            char vChar = Character.toLowerCase(chars[firstVowel]);
            if (vChar == 'u' && firstVowel > 0 && Character.toLowerCase(chars[firstVowel - 1]) == 'q') {
                firstVowel++;
            } else if (vChar == 'i' && firstVowel > 0 && Character.toLowerCase(chars[firstVowel - 1]) == 'g') {
                firstVowel++;
            }
        }

        int vowelCount = 0;
        int[] vowelPositions = new int[n];
        for (int i = firstVowel; i <= lastVowel; i++) {
            if (CHAR_TO_VOWEL_INFO.containsKey(chars[i])) {
                vowelPositions[vowelCount++] = i;
            }
        }

        if (vowelCount == 0) return -1;
        if (vowelCount == 1) return vowelPositions[0];

        // Ưu tiên 1: Nếu có cặp 'ươ' (như nướng, dưới, người), dấu luôn đặt vào 'ơ'
        for (int k = 0; k < vowelCount - 1; k++) {
            int pos1 = vowelPositions[k];
            int pos2 = vowelPositions[k + 1];
            int row1 = CHAR_TO_VOWEL_INFO.get(chars[pos1])[0];
            int row2 = CHAR_TO_VOWEL_INFO.get(chars[pos2])[0];
            if (row1 == 10 && row2 == 8) {
                return pos2;
            }
        }

        // Ưu tiên 2: Nếu có nguyên âm mang dấu phụ (ă, â, ê, ô, ơ, ư), đặt dấu vào nguyên âm đó
        for (int k = 0; k < vowelCount; k++) {
            int pos = vowelPositions[k];
            int row = CHAR_TO_VOWEL_INFO.get(chars[pos])[0];
            if (row == 1 || row == 2 || row == 4 || row == 7 || row == 8 || row == 10) {
                return pos;
            }
        }

        boolean hasTrailingConsonant = (lastVowel < n - 1);

        // Nếu có 3 nguyên âm (VD: oai, uôi, ươi, uyên) -> đặt ở nguyên âm giữa
        if (vowelCount >= 3) {
            return vowelPositions[1];
        }

        // Nếu có 2 nguyên âm:
        // - Có phụ âm cuối (VD: toán, miền, uống) -> nguyên âm thứ 2
        // - Không có phụ âm cuối (VD: hỏi, của, xào, cay, lâu) -> nguyên âm thứ 1
        if (hasTrailingConsonant) {
            return vowelPositions[1];
        } else {
            return vowelPositions[0];
        }
    }

    private static boolean hasVowel(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (CHAR_TO_VOWEL_INFO.containsKey(word.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTonedVowel(String word) {
        for (int i = 0; i < word.length(); i++) {
            int[] info = CHAR_TO_VOWEL_INFO.get(word.charAt(i));
            if (info != null && info[1] > 0) {
                return true;
            }
        }
        return false;
    }

    private static char buildVowelChar(int row, int tone, boolean isUpper) {
        char c = VOWEL_TABLE[row][tone];
        return isUpper ? Character.toUpperCase(c) : c;
    }
}
