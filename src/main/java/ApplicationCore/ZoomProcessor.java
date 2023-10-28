package ApplicationCore;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import static ApplicationCore.ZoomProcessor.linierInterpolation;

public class ZoomProcessor {
    public static final int ZOOMED_IMAGE_SIZE = 400;

    /*Метод заполнения матрицы увеличиваемого участка
    Принимает:
    - Заполняемую матрицу увеличиваемого участка
    - Матрицу яркости картинки
    - Текущую координату Y
    - Текущую координату X
    Контракт метода:
    - Метод принимает только корректные значения X и Y,
    необходимо обеспечить корректную поставку этих значений из вне
    */
    public static void fillZoomBox(int[][] zoomBox ,int[][] lightness ,int currentY, int currentX){
        for (int zoomBoxY = 0; zoomBoxY < zoomBox.length; zoomBoxY++) {
            for (int zoomBoxX = 0; zoomBoxX < zoomBox[zoomBoxY].length; zoomBoxX++) {
                zoomBox[zoomBoxY][zoomBoxX] = lightness[zoomBoxY + currentY][zoomBoxX + currentX];
            }
        }
    }
    /*Метод отрисовки увеличенного изображения с заданным сдвигом
     Принимает:
      - Изображение, на котором будет происходить отрисовка
      - Матрицу яркости увеличенного участка изображения
      - Значение увеличения
      - Сдвиг
      - Флаг - интерполирование
      Алгоритм работы метода:
      - Вычисления яркости со сдвигом
      - Если флаг - интерполирование - интерполируем яркости
      - Отрисовываем изображение по полученным яркостям
    */
    public static void paintZoomedImageWithShift(BufferedImage image, int[][] zoomLightness, int zoom, int shift, boolean isInterpolating){
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int[][] lightnessMatrix = new int[imageHeight][imageWidth];
        for (int YzoomedImage = 0, YzoomBox = 0; YzoomedImage < imageHeight ; YzoomedImage+= zoom, YzoomBox++) {
            for (int XzoomedImage = 0, XzoomBox = 0; XzoomedImage < imageWidth ; XzoomedImage+= zoom, XzoomBox++) {
                int lightness = (zoomLightness[YzoomBox][XzoomBox] >> shift) & 0xFF;;
                for (int y = YzoomedImage; y < YzoomedImage + zoom; y++) {
                    for (int x = XzoomedImage; x < XzoomedImage + zoom; x++) {
                        lightnessMatrix[y][x] = lightness;
                    }
                }
            }
        }
        /*Интерполирование изображения*/
        if(isInterpolating){
            interpolate(lightnessMatrix, zoom);
        }
        /*Вывод изображения на картинку*/
        paintImage(image, lightnessMatrix);
    }

    /*Метод отрисовки увеличенного изображения с нормированием
      Принимает:
      - Изображение, на котором будет происходить отрисовка
      - Матрицу яркости увеличенного участка изображения
      - Значение увеличения
      - Флаг - интерполирование
      Алгоритм работы метода:
      - Выявление максимума, минимума яркости на участке изображения
      - Нормирование яркости
      - Если флаг - интерполирование - интерполируем яркости
      - Отрисовываем изображение по полученным яркостям
    */
    public static void paintZoomedImageWithNormalisation(BufferedImage image, int[][] zoomLightness, int zoom, boolean isInterpolating){
        int min = Arrays.stream(zoomLightness).flatMapToInt(Arrays::stream).min().getAsInt();
        int max = Arrays.stream(zoomLightness).flatMapToInt(Arrays::stream).max().getAsInt();
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        //Создание матрицы яркостей будущего изображения
        int[][] lightnessMatrix = new int[imageHeight][imageWidth];
        double k = 255.0 / (max - min);
        /*Блок заполнения матрицы ргб кодов с нормализацией*/
        for (int YzoomedImage = 0, YzoomBox = 0; YzoomedImage < imageHeight; YzoomedImage+= zoom, YzoomBox++) {
            for (int XzoomedImage = 0, XzoomBox = 0; XzoomedImage < imageWidth; XzoomedImage+= zoom, XzoomBox++) {
                int lightness = (int)((zoomLightness[YzoomBox][XzoomBox] - min) * k);
                for (int y = YzoomedImage; y < YzoomedImage + zoom; y++) {
                    for (int x = XzoomedImage; x < XzoomedImage + zoom; x++) {
                        lightnessMatrix[y][x] = lightness;
                    }
                }
            }
        }
        /*Интерполирование изображения*/
        if(isInterpolating){
            interpolate(lightnessMatrix, zoom);
        }
        /*Вывод изображения на картинку*/
        paintImage(image, lightnessMatrix);
    }

    /*Метод интерполирования изображения
    Принимает:
    - вычисленные ранее значения яркостей в диапазоне от 0 до 255
    в виде матрицы
    - значение увеличения
    Алгоритм работы метода:
    - Последовательно итерируясь по увеличенным пикселям
    производим интерполяцию:
    1. вычисляем угловые значения яркостей
    2. последовательно итерируясь по увеличинному пикселю по строчно
    вычисляем крайнее левое и крайнее правое значения яркостей в строке
    3. далее применяем линейную интерполяцию для всех элементов внутри строки
    4. после интерполирования по угловым значениям у нас остаётся справа
    и снизу полосы, которые не были интерполированы. Их интерполируем отдельно.
    */
    private static void interpolate(int[][] lightnessMatrix, int zoom){
        int I11, I12, I21, I22;
        int x,y;
        for (y = 0; y < lightnessMatrix.length - zoom; y+= zoom) {
            for (x = 0; x < lightnessMatrix.length - zoom; x+= zoom) {
                I11 = lightnessMatrix[y][x];
                I12 = lightnessMatrix[y][x + zoom];
                I21 = lightnessMatrix[y + zoom][x] ;
                I22 = lightnessMatrix[y + zoom][x + zoom];
                double d = I11;
                double a = I12 - d;
                double b = I21 - d;
                double c = I22 - a - b - d;
                for (int yInner = y; yInner < y + zoom; yInner++) {
                    double yLocal = (double) (yInner - y) / zoom;
                    for (int xInner = x; xInner < x + zoom; xInner++) {
                        double xLocal = (double) (xInner - x) / zoom;
                        lightnessMatrix[yInner][xInner] = (int) (a * xLocal + b * yLocal + c * xLocal * yLocal + d);
                    }
                }
            }
            //интерполирование правой полоски
            for (int yInner = y; yInner < y + zoom; yInner++) {
                lightnessMatrix[yInner][x] = (int)(linierInterpolation(0, lightnessMatrix[y][x], zoom, lightnessMatrix[y + zoom][x], yInner - y));
                for (int xInner = x + 1; xInner < x + zoom; xInner++) {
                    lightnessMatrix[yInner][xInner] = lightnessMatrix[yInner][x];
                }
            }
        }
        //интерполирование нижней полоски
        for (x = 0; x < lightnessMatrix.length - zoom; x+= zoom) {
            for (int yInner = y; yInner < y + zoom; yInner++) {
                for (int xInner = x + 1; xInner < x + zoom; xInner++) {
                    lightnessMatrix[yInner][xInner] = (int)(linierInterpolation(0, lightnessMatrix[yInner][x],zoom, lightnessMatrix[yInner][x + zoom], xInner - x));
                }
            }
        }
    }
    /*Метод закраски изображения
    Принимает:
    - Изображение которое нужно закрасить
    - Матрица яркостей для закраски изображения
    */
    private static void paintImage(BufferedImage image, int[][] lightness){
        for (int y = 0; y < lightness.length; y++) {
            for (int x = 0; x < lightness.length; x++) {
                int light = lightness[y][x] & 0xFF;
                image.setRGB(x,y,light << 16 | light << 8 | light);
            }
        }
    }
    /*Метод закраски обзорного изображения
    Принимает:
    - Изображение для закраски
    - Матрицу яркостей
     Алгоритм работы метода:
     - Вычисляется значение зума по соотношению
     размеров матрицы и размеров изображения
     - Далее берётся каждый zoom-ный пиксель и
     со сдвигом 2 закрашивает изображение
    */
    public static void paintOverviewImage(BufferedImage image, int[][] lightness){
        int height = image.getHeight();
        int width = image.getWidth();
        int zoom = lightness.length/ height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x,y,Calculator.calculateRGB(lightness[y * zoom][x * zoom], 2));
            }
        }
        ImageTester.writeImageToFile(image, "overview");
    }
    /*Метод реализующий линейную интерполяцию*/
    static double linierInterpolation(double x1, double y1, double x2, double y2, double x){
        return (x - x1) * (y2 - y1) / (x2 - x1) + y1;
    }

}

class ZoomProcessorTest{
    public static void main(String[] args){
        int[][] pixels = new int[][]{
                {0,0,0,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0},
        };
        int I11 = 0;
        int I12 = 127;
        int I21 = 127;
        int I22 = 255;
        printMatrix(pixels);
        for (int y = 0; y < pixels.length; y++) {
            pixels[y][0] = (int)(linierInterpolation(0, I11, 5, I21, y));
            int sideI = (int)(linierInterpolation(0, I12, 5, I22, y));
            for (int x = 1; x < pixels[y].length; x++) {
                pixels[y][x] = (int)(linierInterpolation(0, pixels[y][0], 5, sideI, x));
            }
        }
        printMatrix(pixels);
        for (int y = 0; y < pixels.length; y++) {
            for (int x = 0; x < pixels[y].length; x++) {
                pixels[y][x] *= 4;
            }
        }
        ImageTester.writeImageToFile(ImageTester.createImage(pixels), "interpolating");
    }
    private static void printMatrix(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            System.out.println(Arrays.toString(matrix[i]));
        }
    }
}
