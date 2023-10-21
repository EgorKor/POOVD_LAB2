package ApplicationCore;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageTester {
    public static void main(String[] args){
        /*writeImageToFile(createImage(ZoomProcessor.createZoomBox(createTestMBVLightnessMatrix(), 0,0,4))
                , "zoomedBox");
        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_3BYTE_BGR);
        ZoomProcessor.paintZoomedImage(image,
                ZoomProcessor.createZoomBox(createTestMBVLightnessMatrix(), 0, 0, 4),
                4);*/
//        writeImageToFile(image, "zoomedImage");
//        writeImageToFile(, "zoomedImage");
        int[][] ints = new int[][]{
                {1,2,3},
                {5,6,7}
        };
        System.out.println(Arrays.stream(ints).flatMapToInt(Arrays::stream).max().getAsInt());
        System.out.println(Arrays.stream(ints).flatMapToInt(Arrays::stream).min().getAsInt());
    }

    public static void writeImageToFile(BufferedImage image, String fileName){
        try {
            fileName += ".jpg";
            ImageIO.write(image, "jpg", new File(fileName));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static int[][] createTestMBVLightnessMatrix(){
        try{
            return LightnessMatrixCreator.createMatrix(IOUtils.toByteArray(new FileInputStream("C:\\Users\\Егор\\Documents\\RVx5200.mbv")));
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static BufferedImage createTestMBVImage(){
        try {
            int[][] matrix = LightnessMatrixCreator.createMatrix(IOUtils.toByteArray(new FileInputStream("C:\\Users\\Егор\\Documents\\RVx5200.mbv")));
            BufferedImage image = new BufferedImage(matrix[0].length, matrix.length, BufferedImage.TYPE_3BYTE_BGR);
            for (int y = 0; y < matrix.length; y++) {
                for (int x = 0; x < matrix[y].length; x++) {
                    image.setRGB(x,y, Calculator.calculateRGB(matrix[y][x], 0));
                }
            }
            return image;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage createImage(int[][] lightness, int shift){
        BufferedImage image = new BufferedImage(lightness[0].length, lightness.length, BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < lightness.length; y++) {
            for (int x = 0; x < lightness[y].length; x++) {
                image.setRGB(x,y,Calculator.calculateRGB(lightness[y][x], shift));
            }
        }
        return image;
    }

    public static BufferedImage createImage(int[][] lightness){
        BufferedImage image = new BufferedImage(lightness[0].length, lightness.length, BufferedImage.TYPE_3BYTE_BGR);
        int currentRangeMin = 0;
        int currentRangeMax = 1023;
        int maxCurrentRangeDiffMinCurrentRange = currentRangeMax - currentRangeMin;
        int newRangeMax = 255;
        int newRangeMaxDiffCurrentRangeMin = newRangeMax - currentRangeMin;
        for (int y = 0; y < lightness.length; y++) {
            for (int x = 0; x < lightness[y].length; x++) {
                int light = (newRangeMaxDiffCurrentRangeMin * lightness[y][x])
                        / maxCurrentRangeDiffMinCurrentRange + currentRangeMin;
                int rgb = (0b11111111 << 24) | (light << 16) | (light << 8) | light;
                image.setRGB(x,y,rgb);
            }
        }
        return image;
    }
}
