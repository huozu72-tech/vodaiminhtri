package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.data.repository.MenuRepositoryImpl;
import com.example.chieuthu3_ketthucmonhoc.domain.models.ChatMessage;
import com.example.chieuthu3_ketthucmonhoc.domain.models.FoodCategory;
import com.example.chieuthu3_ketthucmonhoc.domain.models.MenuItem;
import com.example.chieuthu3_ketthucmonhoc.domain.models.TasteAttribute;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AIChatbotServiceImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AIChatbotServiceTest {

    private AIChatbotServiceImpl chatbotService;
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
        chatbotService = new AIChatbotServiceImpl(menuRepository);
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
}
