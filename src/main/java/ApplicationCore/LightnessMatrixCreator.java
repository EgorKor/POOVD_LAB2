package ApplicationCore;
/*
Класс LightnessMatrixCreator, главная функция - класса создать матрицу яркости
из массива байт - файла формата mbv
*/
public class LightnessMatrixCreator {
    /*
    Метод createMatrix - создаёт матрицу яркостей, принимает на вход
    массив байт, из которых считывает размер матрицы (ширину и высоту).
    */
    public static int[][] createMatrix(byte[] fileData){
        /*вызываем функцию calculateSize для расчёта размера матрицы*/
        int width = Calculator.calculateSize(fileData[0],fileData[1]);//расчёт ширины
        int height = Calculator.calculateSize(fileData[2], fileData[3]);//расчёт высоты
        int[][] lightnessMatrix = new int[height][width];//создали матрицу
        int fileDataIndex = 4;//переменная для итерирования по массиву байт
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                /*Заполняем матрицу построчно, последовательно беря пары байт из массива*/
                /*Порядок расчёта значения яркости:
                0. При работе с байтом его необходимо предварительно "обрезать"
                логическим умножением на 255 для очищения записи байта в формате int от мусорных битов,
                т.к. в процессе операций происходит неявное преобразование типа byte к int.
                1. Берём второй байт и сдвигаем его на 8 бит влево
                2. Берём первый байт и накладываем его на второй побитовой дизъюнкцией
                3. Результат побитовым логическим сложением обрезаем до 10 младших битов 
                * */
                lightnessMatrix[i][j]  = ((fileData[fileDataIndex + 1] & 0xFF) << 8
                        | (fileData[fileDataIndex] & 0xFF)) & 0b1111111111;
                /*сдвигаем переменную итератор на следующую пару байт*/
                fileDataIndex += 2;
            }
        }
        /*возвращаем матрицу из метода*/
        return lightnessMatrix;
    }
}
