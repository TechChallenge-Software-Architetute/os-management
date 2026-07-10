package com.os.workshop.domain.client;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Nested
    class CpfDetection {

        @Test
        void detectsCpfBy11Digits() {
            Document doc = Document.of("52998224725");
            assertEquals(DocumentType.CPF, doc.getType());
            assertEquals("52998224725", doc.getValue());
        }

        @Test
        void detectsCpfWithFormatting() {
            Document doc = Document.of("529.982.247-25");
            assertEquals(DocumentType.CPF, doc.getType());
            assertEquals("52998224725", doc.getValue());
        }

        @Test
        void formattedCpf() {
            Document doc = Document.of("52998224725");
            assertEquals("529.982.247-25", doc.formatted());
        }
    }

    @Nested
    class CnpjDetection {

        @Test
        void detectsCnpjBy14Digits() {
            Document doc = Document.of("11222333000181");
            assertEquals(DocumentType.CNPJ, doc.getType());
            assertEquals("11222333000181", doc.getValue());
        }

        @Test
        void detectsCnpjWithFormatting() {
            Document doc = Document.of("11.222.333/0001-81");
            assertEquals(DocumentType.CNPJ, doc.getType());
            assertEquals("11222333000181", doc.getValue());
        }

        @Test
        void formattedCnpj() {
            Document doc = Document.of("11222333000181");
            assertEquals("11.222.333/0001-81", doc.formatted());
        }
    }

    @Nested
    class Validation {

        @Test
        void throwsWhenNull() {
            assertThrows(IllegalArgumentException.class, () -> Document.of(null));
        }

        @Test
        void throwsWhenInvalidLength() {
            assertThrows(IllegalArgumentException.class, () -> Document.of("123456"));
        }

        @Test
        void throwsWhenInvalidCpfDigits() {
            assertThrows(IllegalArgumentException.class, () -> Document.of("12345678901"));
        }

        @Test
        void throwsWhenInvalidCnpjDigits() {
            assertThrows(IllegalArgumentException.class, () -> Document.of("12345678901234"));
        }

        @Test
        void throwsWhenAllSameDigitsCpf() {
            assertThrows(IllegalArgumentException.class, () -> Document.of("11111111111"));
        }

        @Test
        void throwsWhenAllSameDigitsCnpj() {
            assertThrows(IllegalArgumentException.class, () -> Document.of("11111111111111"));
        }
    }

    @Nested
    class Equality {

        @Test
        void equalDocumentsAreEqual() {
            Document a = Document.of("52998224725");
            Document b = Document.of("529.982.247-25");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void differentDocumentsAreNotEqual() {
            Document a = Document.of("52998224725");
            Document b = Document.of("11222333000181");
            assertNotEquals(a, b);
        }
    }
}
