package ApplicationCore;

/*Класс Calculator, цель класса предоставить методы:
- вычисления кода RGB по яркости с заданным сдвигом
- вычисление размера матрицы яркостей по двум байтам*/
public class Calculator {
    /*
    Метод calculateRGB принимает два байта firstByte и secondByte и сдвиг битов shift
    метод вычисляет rgb код и возвращает его в формате int
    */
    public static int calculateRGB(int lightness, int shift){
        lightness = (lightness >> shift) & 0xFF;//Вычисляем яркость со сдвигом и обрезаем всё до младших 8 бит
        return (0b11111111 << 24) | (lightness << 16) | (lightness << 8) | lightness;//формируем rgb код
    }
    /*
    Метод calculateSize принимает два байта firstByte и secondByte
    метод вычисляет значение размера по этим двум байтам и возвращает его в формате int
    */
    public static int calculateSize(byte firstByte, byte secondByte){
        int size = secondByte & 0xFF; //обрезаем всё кроме 8 младших разрядов второго байта
        return (size << 8) | firstByte & 0xFF;//сдвигаем текущие биты на 8 разрядов и накладываем первый байт
    }
}
