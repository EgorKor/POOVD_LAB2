package ApplicationCore;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.Arrays;

public class ZoomProcessor {
    public static final int ZOOMED_IMAGE_SIZE = 400;
    public static void fillZoomBox(int[][] zoomBox ,int[][] lightness ,int currentY, int currentX, int zoom){
        for (int zoomBoxY = 0; zoomBoxY < zoomBox.length; zoomBoxY++) {
            for (int zoomBoxX = 0; zoomBoxX < zoomBox[zoomBoxY].length; zoomBoxX++) {
                zoomBox[zoomBoxY][zoomBoxX] = lightness[zoomBoxY + currentY][zoomBoxX + currentX];
            }
        }
    }

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
        if(isInterpolating){
            int I11;
            int I12;
            int I21;
            int I22;
            int y;
            int x;
            for (y = 0; y < imageHeight - zoom; y+= zoom) {
                for (x = 0; x < imageWidth - zoom; x+= zoom) {
                    I11 = lightnessMatrix[y][x] & 0xFF;
                    I12 = lightnessMatrix[y][x + zoom]& 0xFF ;
                    I21 = lightnessMatrix[y + zoom][x] & 0xFF;
                    I22 = lightnessMatrix[y + zoom][x + zoom]& 0xFF ;
                    for (int yInner = y; yInner < y + zoom; yInner++) {
                        lightnessMatrix[yInner][x] = (int)(YbyXlinierInterpolation(0, I11 , zoom, I21, yInner - y));
                        lightnessMatrix[yInner][x + zoom - 1] = (int)(YbyXlinierInterpolation(0, I12, zoom, I22, yInner - y));
                        for (int xInner = x + 1; xInner < x + zoom - 1; xInner++) {
                            lightnessMatrix[yInner][xInner] = (int)YbyXlinierInterpolation(0, lightnessMatrix[yInner][x], zoom - 1, lightnessMatrix[yInner][x + zoom - 1], xInner - x) ;
                        }
                    }
                }
                //интерполирование правой полоски
                for (int yInner = y; yInner < y + zoom; yInner++) {
                    lightnessMatrix[yInner][x] = (int)(YbyXlinierInterpolation(0, lightnessMatrix[y][x], zoom, lightnessMatrix[y + zoom][x], yInner - y));
                    for (int xInner = x + 1; xInner < x + zoom; xInner++) {
                        lightnessMatrix[yInner][xInner] = lightnessMatrix[yInner][x];
                    }
                }
            }
            //интерполирование нижней полоскию
            for (x = 0; x < imageWidth - zoom; x+= zoom) {
                for (int yInner = y; yInner < y + zoom; yInner++) {
                    for (int xInner = x + 1; xInner < x + zoom; xInner++) {
                        lightnessMatrix[yInner][xInner] = (int)(YbyXlinierInterpolation(0, lightnessMatrix[yInner][x],zoom, lightnessMatrix[yInner][x + zoom], xInner - x));
                    }
                }
            }
        }
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int light = lightnessMatrix[y][x];
                image.setRGB(x,y,light << 16 | light << 8 | light);
            }
        }
    }

    public static void paintZoomedImageWithNormalisation(BufferedImage image, int[][] zoomLightness, int zoom, boolean isInterpolating){
        int min = Arrays.stream(zoomLightness).flatMapToInt(Arrays::stream).min().getAsInt();
        int max = Arrays.stream(zoomLightness).flatMapToInt(Arrays::stream).max().getAsInt();
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
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
        /*Блок интерполирования*/
        if(isInterpolating){
            int I11;
            int I12;
            int I21;
            int I22;
            int y;
            int x;
            for (y = 0; y < imageHeight - zoom; y+= zoom) {
                for (x = 0; x < imageWidth - zoom; x+= zoom) {
                        I11 = lightnessMatrix[y][x];
                        I12 = lightnessMatrix[y][x + zoom];
                        I21 = lightnessMatrix[y + zoom][x] ;
                        I22 = lightnessMatrix[y + zoom][x + zoom];
                        for (int yInner = y; yInner < y + zoom; yInner++) {
                            lightnessMatrix[yInner][x] = (int)(YbyXlinierInterpolation(0, I11 , zoom, I21, yInner - y));
                            lightnessMatrix[yInner][x + zoom - 1] = (int)(YbyXlinierInterpolation(0, I12, zoom, I22, yInner - y));
                            for (int xInner = x + 1; xInner < x + zoom - 1; xInner++) {
                                lightnessMatrix[yInner][xInner] = (int)YbyXlinierInterpolation(0, lightnessMatrix[yInner][x], zoom - 1, lightnessMatrix[yInner][x + zoom - 1], xInner - x) ;
                            }
                        }
                }
                //интерполирование правой полоски
                for (int yInner = y; yInner < y + zoom; yInner++) {
                    lightnessMatrix[yInner][x] = (int)(YbyXlinierInterpolation(0, lightnessMatrix[y][x], zoom, lightnessMatrix[y + zoom][x], yInner - y));
                    for (int xInner = x + 1; xInner < x + zoom; xInner++) {
                        lightnessMatrix[yInner][xInner] = lightnessMatrix[yInner][x];
                    }
                }
            }
            //интерполирование нижней полоскию
            for (x = 0; x < imageWidth - zoom; x+= zoom) {
                for (int yInner = y; yInner < y + zoom; yInner++) {
                    for (int xInner = x + 1; xInner < x + zoom; xInner++) {
                        lightnessMatrix[yInner][xInner] = (int)(YbyXlinierInterpolation(0, lightnessMatrix[yInner][x],zoom, lightnessMatrix[yInner][x + zoom], xInner - x));
                    }
                }
            }
        }
        /*Блок вывода изображения на картинку*/
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int light = lightnessMatrix[y][x] & 0xFF;
                image.setRGB(x,y,light << 16 | light << 8 | light);
            }
        }
    }

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

    public static void main(String[] args){
        int[][] pixels = new int[][]{
                {0,0,0,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0},
                {0,0,0,0,0},
        };
        int I11 = 220;
        int I12 = 250;
        int I21 = 150;
        int I22 = 200;
        printMatrix(pixels);
        for (int y = 0; y < pixels.length; y++) {
            pixels[y][0] = (int)(YbyXlinierInterpolation(0, I11, 5, I21, y));
            pixels[y][pixels.length - 1] = (int)(YbyXlinierInterpolation(0, I12, 5, I22, y));
            for (int x = 1; x < pixels[y].length - 1; x++) {
                pixels[y][x] = (int)(YbyXlinierInterpolation(0, pixels[y][0], 4, pixels[y][pixels.length - 1], x));
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

    private static double YbyXlinierInterpolation(double x1, double y1, double x2, double y2, double x){
        return (x - x1) * (y2 - y1) / (x2 - x1) + y1;
    }

    private static double XbyYlinierInterpolation(double x1, double y1, double x2, double y2, double y){
        return (y - y1) * (x2 - x1 )/(y2 - y1) + x1;
    }
    private static void printMatrix(int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            System.out.println(Arrays.toString(matrix[i]));
        }
    }
}
