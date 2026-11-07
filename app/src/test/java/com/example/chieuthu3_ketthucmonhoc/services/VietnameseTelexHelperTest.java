package com.example.chieuthu3_ketthucmonhoc.services;

import com.example.chieuthu3_ketthucmonhoc.utils.VietnameseTelexHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VietnameseTelexHelperTest {

    @Test
    public void convertsStandardTelexSentencesIntoAccentedVietnamese() {
        assertEquals(
                "tìm món cay ngon dưới 100k",
                VietnameseTelexHelper.convertTelexSentence("timf mons cay ngon duowis 100k")
        );
        assertEquals(
                "gợi ý lẩu gà lá giang",
                VietnameseTelexHelper.convertTelexSentence("gowij ys laaur gaf las giang")
        );
        assertEquals(
                "chân giò muối chiên giòn",
                VietnameseTelexHelper.convertTelexSentence("chaan giof muoois chieen gionf")
        );
        assertEquals(
                "đi 4 người ăn món nướng gì",
                VietnameseTelexHelper.convertTelexSentence("ddi 4 nguowif awn mons nuowngs gif")
        );
    }

    @Test
    public void supportsFreeToneOrderAndCompoundUoW() {
        // Tone typed before final consonant vs at the end of word
        assertEquals("món", VietnameseTelexHelper.convertTelexSentence("mosn"));
        assertEquals("món", VietnameseTelexHelper.convertTelexSentence("mons"));
        assertEquals("nướng", VietnameseTelexHelper.convertTelexSentence("nuowngs"));
        assertEquals("nướng", VietnameseTelexHelper.convertTelexSentence("nuongws"));
        assertEquals("dưới", VietnameseTelexHelper.convertTelexSentence("duowis"));
        assertEquals("dưới", VietnameseTelexHelper.convertTelexSentence("duoiws"));
        assertEquals("quán", VietnameseTelexHelper.convertTelexSentence("quans"));
    }

    @Test
    public void preservesAlreadyAccentedUnicodeAndSupportsUndoOnDoubleTap() {
        assertEquals(
                "Tìm món cay dưới 100k",
                VietnameseTelexHelper.convertTelexSentence("Tìm món cay dưới 100k")
        );
        // Double tapping tone key 's' undoes the tone and appends 's'
        assertEquals("mons", VietnameseTelexHelper.convertTelexSentence("monss"));
    }
}
