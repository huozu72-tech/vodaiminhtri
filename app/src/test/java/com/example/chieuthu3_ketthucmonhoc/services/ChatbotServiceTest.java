package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.datasource.InitialDataFactory;
import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.ChatbotServiceImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.services.ChatbotMLModel;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChatbotServiceTest {

    private ChatbotServiceImpl chatbotService;
    private MenuRepositoryImpl menuRepository;

    private final MenuItem dishSpicy = new MenuItem(
            1, "Chân Gà Sốt Thái", "Chua cay", 65000L, FoodCategory.SIDE_DISH,
            Arrays.asList(TasteAttribute.SPICY, TasteAttribute.SOUR), true, 4.8, 0, 0
    );
    private final MenuItem dishCrispy = new MenuItem(
            2, "Chân Giò Muối Chiên Giòn", "Giòn rụm", 120000L, FoodCategory.MAIN_DISH,
            Collections.singletonList(TasteAttribute.CRISPY), true, 4.8, 0, 0
    );
    private final MenuItem dishCheap = new MenuItem(
            3, "7Up", "Nước ngọt", 15000L, FoodCategory.BEVERAGE,
            Collections.singletonList(TasteAttribute.REFRESHING), true, 4.8, 0, 0
    );

    @Before
    public void setup() {
        menuRepository = new MenuRepositoryImpl(Arrays.asList(dishSpicy, dishCrispy, dishCheap));
        chatbotService = new ChatbotServiceImpl(menuRepository);
    }

    @Test
    public void initialChatContainsWelcomeMessage() {
        List<ChatMessage> history = chatbotService.getChatHistory();
        assertEquals(1, history.size());
        assertFalse(history.get(0).isUser());
    }

    @Test
    public void budgetQueryRecommendsItemsWithinLimit() {
        ChatMessage response = chatbotService.sendMessage("Có món nào dưới 50k không bạn?");
        assertFalse(response.isUser());
        assertFalse(response.getSuggestedItems().isEmpty());
        for (MenuItem item : response.getSuggestedItems()) {
            assertTrue(item.getPrice() <= 50000);
        }
        assertEquals(dishCheap.getId(), response.getSuggestedItems().get(0).getId());
    }

    @Test
    public void tasteQueryMatchesSpicyTasteAttribute() {
        ChatMessage response = chatbotService.sendMessage("Tôi thích ăn món cay");
        assertFalse(response.isUser());
        assertTrue(response.getText().contains("cay"));
        assertEquals(dishSpicy.getId(), response.getSuggestedItems().get(0).getId());
    }

    @Test
    public void imageAnalysisMatchesFoodByHint() {
        ChatMessage response = chatbotService.analyzeFoodImage("anh_mon_an_chan_ga.jpg");
        assertFalse(response.isUser());
        assertFalse(response.getSuggestedItems().isEmpty());
        assertEquals(dishSpicy.getId(), response.getSuggestedItems().get(0).getId());
    }

    @Test
    public void imageAnalysisWithUnmatchedHintAdmitsNoMatchInsteadOfClaimingSuccess() {
        ChatMessage response = chatbotService.analyzeFoodImage("anh_mon_an_chan_ga.jpg");

        assertFalse(response.isUser());
        // Hint không chứa từ khóa có dấu -> phải nói thật là chưa nhận diện được
        assertTrue(response.getText().contains("chưa nhận diện"));
        assertFalse(response.getSuggestedItems().isEmpty());
    }

    @Test
    public void blankMessageIsRejectedWithoutAddingEmptyUserBubble() {
        ChatMessage response = chatbotService.sendMessage("   ");

        assertFalse(response.isUser());
        assertTrue(response.getText() != null && !response.getText().trim().isEmpty());
        List<ChatMessage> history = chatbotService.getChatHistory();
        assertEquals(2, history.size());
        for (ChatMessage msg : history) {
            assertFalse(msg.isUser());
        }
    }

    @Test
    public void budgetBelowEveryItemPriceReturnsHonestNoMatchAnswerWithCheapestItem() {
        ChatMessage response = chatbotService.sendMessage("Có món nào dưới 1k không?");

        assertFalse(response.isUser());
        assertTrue(response.getText().contains("không có món"));
        assertEquals(1, response.getSuggestedItems().size());
        assertEquals(dishCheap.getId(), response.getSuggestedItems().get(0).getId());
    }

    @Test
    public void fullSevenDigitVndAmountIsParsedAsOneMillionNotOneThousand() {
        ChatMessage response = chatbotService.sendMessage("Có món nào dưới 1000000 không?");

        assertFalse(response.isUser());
        assertTrue(response.getText().contains("1000k"));
        boolean containsCrispy = false;
        for (MenuItem item : response.getSuggestedItems()) {
            if (item.getId() == dishCrispy.getId()) {
                containsCrispy = true;
            }
            assertTrue(item.getPrice() <= 1000000L);
        }
        assertTrue(containsCrispy);
    }

    @Test
    public void keywordSearchReturnsMatchingItems() {
        ChatMessage response = chatbotService.sendMessage("Chân");
        assertFalse(response.isUser());
        assertFalse(response.getSuggestedItems().isEmpty());
        for (MenuItem item : response.getSuggestedItems()) {
            assertTrue(item.getName().toLowerCase().contains("chân"));
        }
    }

    @Test
    public void bestDishQueryReturnsMainDishOnly() {
        ChatMessage response = chatbotService.sendMessage("tôi muốn ăn món ngon nhất");
        assertFalse(response.isUser());
        assertEquals(1, response.getSuggestedItems().size());
        assertEquals(FoodCategory.MAIN_DISH, response.getSuggestedItems().get(0).getCategory());
    }

    @Test
    public void specialtyDishQueryReturnsMainOrSideDishOnly() {
        ChatMessage response = chatbotService.sendMessage("tôi muốn ăn món đặc sản của nhà hàng");
        assertFalse(response.isUser());
        assertEquals(1, response.getSuggestedItems().size());
        FoodCategory cat = response.getSuggestedItems().get(0).getCategory();
        assertTrue(cat == FoodCategory.MAIN_DISH || cat == FoodCategory.SIDE_DISH);
        assertTrue(cat != FoodCategory.BEVERAGE);
    }


    @Test
    public void mlModelAchievesHighAccuracyAndValidProbabilityDistribution() {
        ChatbotMLModel model = chatbotService.getMlModel();
        assertNotNull(model);
        assertTrue("Vocabulary phải được trích xuất từ tập huấn luyện", model.getVocabularySize() > 50);
        assertTrue("Độ chính xác tập huấn luyện phải >= 92%", model.evaluateTrainingAccuracy() >= 0.92);

        List<ChatbotMLModel.TrainingSample> validationSamples = Arrays.asList(
                new ChatbotMLModel.TrainingSample("xin chào trợ lý nhà hàng", ChatbotMLModel.Intent.GREETING_HELP),
                new ChatbotMLModel.TrainingSample("tìm món giá dưới 90k", ChatbotMLModel.Intent.BUDGET_QUERY),
                new ChatbotMLModel.TrainingSample("mình thích ăn món chua cay", ChatbotMLModel.Intent.TASTE_QUERY),
                new ChatbotMLModel.TrainingSample("cho xem thực đơn nước uống giải khát", ChatbotMLModel.Intent.CATEGORY_QUERY),
                new ChatbotMLModel.TrainingSample("quán có món gà nướng mật ong không", ChatbotMLModel.Intent.DISH_SEARCH),
                new ChatbotMLModel.TrainingSample("đi 4 người nên gọi combo gì", ChatbotMLModel.Intent.COMBO_CONSULT),
                new ChatbotMLModel.TrainingSample("món nào đặc sản bán chạy nhất quán", ChatbotMLModel.Intent.BESTSELLER_QUERY),
                new ChatbotMLModel.TrainingSample("quán có bán pizza hay trà sữa không", ChatbotMLModel.Intent.OUT_OF_SCOPE)
        );
        assertTrue("Độ chính xác tập kiểm thử độc lập phải >= 85%", model.evaluateAccuracy(validationSamples) >= 0.85);

        ChatbotMLModel.IntentPrediction pred = model.predictIntent("đi 4 người nên gọi combo gì");
        double probSum = 0.0;
        for (double p : pred.getProbabilities().values()) {
            probSum += p;
        }
        assertEquals(1.0, probSum, 0.001);
        assertEquals(ChatbotMLModel.Intent.COMBO_CONSULT, pred.getIntent());
    }

    @Test
    public void combinedBudgetAndTasteQueryFiltersBothPriceAndSpicyTaste() {
        // Kiểm tra đúng câu gợi ý mẫu trên giao diện: "Tìm món cay ngon dưới 100k"
        ChatMessage response = chatbotService.sendMessage("Tìm món cay ngon dưới 100k");
        assertFalse(response.isUser());
        assertFalse(response.getSuggestedItems().isEmpty());
        for (MenuItem item : response.getSuggestedItems()) {
            assertTrue("Món gợi ý phải <= 100k", item.getPrice() <= 100000L);
            assertTrue("Món gợi ý phải có vị cay (không trả về nước ngọt 7Up)", item.getTasteAttributes().contains(TasteAttribute.SPICY));
        }
        assertEquals(dishSpicy.getId(), response.getSuggestedItems().get(0).getId());
    }

    @Test
    public void unaccentedVietnameseQueriesWorkForTasteCategoryAndDishSearch() {
        ChatbotServiceImpl fullMenuChatbot = new ChatbotServiceImpl(
                new MenuRepositoryImpl(InitialDataFactory.createInitialMenuItems())
        );

        ChatMessage crispyResp = fullMenuChatbot.sendMessage("tim mon chien gion");
        assertFalse(crispyResp.getSuggestedItems().isEmpty());
        assertTrue(crispyResp.getSuggestedItems().get(0).getTasteAttributes().contains(TasteAttribute.CRISPY));

        ChatMessage drinkResp = fullMenuChatbot.sendMessage("cho xem do uong giai khat");
        assertFalse(drinkResp.getSuggestedItems().isEmpty());
        assertEquals(FoodCategory.BEVERAGE, drinkResp.getSuggestedItems().get(0).getCategory());

        ChatMessage dishResp = fullMenuChatbot.sendMessage("lau ga la giang gia bao nhieu");
        assertFalse(dishResp.getSuggestedItems().isEmpty());
        assertTrue(dishResp.getSuggestedItems().get(0).getName().contains("Lẩu Gà Lá Giang"));
    }

    @Test
    public void comboGroupConsultationAndOutOfScopeGuardrailWorkCorrectly() {
        ChatbotServiceImpl fullMenuChatbot = new ChatbotServiceImpl(
                new MenuRepositoryImpl(InitialDataFactory.createInitialMenuItems())
        );

        ChatMessage comboResp = fullMenuChatbot.sendMessage("Đi 4 người nên gọi combo món gì?");
        assertFalse(comboResp.isUser());
        assertTrue(comboResp.getText().contains("4 người"));
        assertTrue(comboResp.getSuggestedItems().size() >= 3);

        ChatMessage outResp = fullMenuChatbot.sendMessage("Quán có bán pizza hải sản không?");
        assertFalse(outResp.isUser());
        assertTrue(outResp.getText().contains("pizza"));
        assertFalse(outResp.getSuggestedItems().isEmpty());
    }

    @Test
    public void imageAnalysisRecognizesCookingStyleAndDishHints() {
        ChatbotServiceImpl fullMenuChatbot = new ChatbotServiceImpl(
                new MenuRepositoryImpl(InitialDataFactory.createInitialMenuItems())
        );

        // Kiểm tra nhãn mặc định từ nút Camera trong khung chat
        ChatMessage cameraResp = fullMenuChatbot.analyzeFoodImage("thực đơn món nướng và món chiên giòn");
        assertFalse(cameraResp.isUser());
        assertTrue("Phải nhận diện thành công món nướng & chiên giòn", cameraResp.getText().contains("độ tin cậy"));
        assertFalse(cameraResp.getSuggestedItems().isEmpty());
    }

    @Test
    public void uploadedPhotoOverloadStoresImageUriAndRecognizesVisualCues() {
        ChatbotServiceImpl fullMenuChatbot = new ChatbotServiceImpl(
                new MenuRepositoryImpl(InitialDataFactory.createInitialMenuItems())
        );

        String savedPath = "/data/user/0/com.example.chieuthu3_ketthucmonhoc/files/food_scans/scan_demo.jpg";
        ChatMessage botResp = fullMenuChatbot.analyzeFoodImage(
                "IMG_20261114.jpg",
                "gà nướng mật ong món nướng chiên vàng",
                savedPath
        );

        assertFalse(botResp.isUser());
        assertTrue(botResp.getText().contains("độ tin cậy"));
        assertFalse(botResp.getSuggestedItems().isEmpty());

        List<ChatMessage> history = fullMenuChatbot.getChatHistory();
        ChatMessage userPhotoMsg = history.get(history.size() - 2);
        assertTrue(userPhotoMsg.isUser());
        assertTrue(userPhotoMsg.getText().contains("IMG_20261114.jpg"));
        assertEquals(savedPath, userPhotoMsg.getImageUri());
    }
}
