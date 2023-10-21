package ApplicationGUI;

import static ApplicationCore.Calculator.*;
import static ApplicationCore.LightnessMatrixCreator.createMatrix;

import ApplicationCore.ZoomProcessor;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static javax.swing.JOptionPane.showMessageDialog;

public class GUI extends JFrame {
    /*Комоненты GUI*/
    private JButton fileLoadButton;
    private JTextArea scrollStepTextArea;
    private JButton enterScrollStepButton;
    private JRadioButton a0RadioButton;
    private JRadioButton a1RadioButton;
    private JRadioButton a2RadioButton;
    private JPanel mainPanel;
    private JPanel headerMenuPanel;
    private JScrollPane imageScrollPane;
    private JPanel footerCoordlPanel;
    private JLabel coordLabel;
    private JLabel yCoordLabel;
    private JLabel xCoordLabel;
    private JLabel lightnessLabel;
    private JLabel yValueLabel;
    private JLabel xValueLabel;
    private JLabel lightnessValueLabel;
    private JPanel optionsPanel;
    private JPanel setterScrollPanel;
    private JPanel loadFilePanel;
    private JLabel visualLabel;
    private JLabel loadFileLabel;
    private JLabel loadedFileLabel;
    private JLabel scrollStepLabel;
    private JLabel loadedFileNameLabel;
    private JPanel zoomImagePanel;
    private JButton incrementZoomButton;
    private JButton decrementZoomButton;
    private JRadioButton normRadioButton;
    private JRadioButton interpolRadioButton;
    private JPanel zoomConfigPanel;
    private JLabel zoomValueLabel;
    private JScrollPane overviewImagePane;
    private JScrollPane zoomedImagePane;
    private JLabel imageLabel;
    private JLabel overviewImageLabel;
    private JLabel zoomedImageLabel;
    private JRadioButton wasChosenBeforeButton;
    /*Переменные для логики приложения*/
    private boolean isFileLoaded;
    private boolean isInterpolating;
    private boolean isNormalisation;
    private int[][] lightnessMatrix;
    private int imageHeight;
    private int imageWidth;
    private int[][] zoomBox;
    private int zoomBoxSize;
    private BufferedImage image;
    private ImageIcon imageIcon;


    private BufferedImage zoomedImage;
    private ImageIcon zoomedImageIcon;



    private int[] zoomValues;
    private int zoomIndex;

    private BufferedImage overviewImage;
    private ImageIcon overviewImageIcon;
    private int currentShift;
    private int currentScrollbarPos;

    public GUI() {
        zoomIndex = 0;
        zoomValues = new int[]{1,2,3,4,5,6,7,8,9,10, 20, 30, 40, 50};
        zoomBoxSize = ZoomProcessor.ZOOMED_IMAGE_SIZE/zoomValues[zoomIndex];
        zoomBox = new int[zoomBoxSize][zoomBoxSize];
        overviewImage = new BufferedImage(100,600, BufferedImage.TYPE_3BYTE_BGR);
        zoomedImage = new BufferedImage(zoomBoxSize * zoomValues[zoomIndex], zoomBoxSize * zoomValues[zoomIndex], BufferedImage.TYPE_3BYTE_BGR);
        imageScrollPane.getVerticalScrollBar().setUnitIncrement(100);
        imageLabel = new JLabel();
        zoomedImageLabel = new JLabel();
        zoomedImageIcon = new ImageIcon();
        overviewImageLabel = new JLabel();
        overviewImageIcon = new ImageIcon();
        imageIcon = new ImageIcon();
        imageLabel.setIcon(imageIcon);
        overviewImageLabel.setIcon(overviewImageIcon);
        zoomedImageLabel.setIcon(zoomedImageIcon);
        ButtonGroup group = new ButtonGroup();
        group.add(a0RadioButton);
        group.add(a1RadioButton);
        group.add(a2RadioButton);
        a0RadioButton.setSelected(true);
        wasChosenBeforeButton = a0RadioButton;
        imageScrollPane.getVerticalScrollBar().addAdjustmentListener(
                e -> currentScrollbarPos = e.getValue()
        );
        scrollStepTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char pressedKey = e.getKeyChar();
                if (!String.valueOf(pressedKey).matches("[0-9]")) {
                    e.consume();
                }
                if (scrollStepTextArea.getText().contains("\n")) {
                    scrollStepTextArea.setText(scrollStepTextArea.getText().replaceAll("\n", ""));
                }
            }
        });
        enterScrollStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String stepText = scrollStepTextArea.getText();
                if (!stepText.isEmpty() && stepText.matches("\\d+") && stepText.length() <= 10) {
                    int step = Integer.parseInt(stepText);
                    if (step > 0 && step <= 100) {
                        imageScrollPane.getVerticalScrollBar().setUnitIncrement(step);
                        return;
                    }
                }
                scrollStepTextArea.setText("100");
                showMessageDialog(null, "Ошибка - значение шага должно быть от 1 до 100!\nТекущее значение шага - 100");
            }
        });

        imageScrollPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isFileLoaded) {
                    Point mousePosition = imageScrollPane.getMousePosition();
                    if (mousePosition != null) {
                        int x = mousePosition.x;
                        int y = mousePosition.y;
                        if (x  < imageWidth)
                            xValueLabel.setText(String.valueOf(x));
                        if (y + currentScrollbarPos < imageHeight) {
                            yValueLabel.setText(String.valueOf(y + currentScrollbarPos));
                        }
                        if (yValueLabel.getText().matches("\\d+") && xValueLabel.getText().matches("\\d+")) {
                            lightnessValueLabel.setText(
                                    String.valueOf(lightnessMatrix
                                            [Integer.parseInt(yValueLabel.getText())]
                                            [Integer.parseInt(xValueLabel.getText())]));
                        }
                        y = Integer.parseInt(yValueLabel.getText());
                        x = Integer.parseInt(xValueLabel.getText());
                        if(x + zoomBoxSize < imageWidth - 1 && y + zoomBoxSize < imageHeight - 1){
                            ZoomProcessor.fillZoomBox(zoomBox,lightnessMatrix,
                                    y, x,
                                    zoomValues[zoomIndex]);
                            if(isNormalisation){
                                ZoomProcessor.paintZoomedImageWithNormalisation(zoomedImage, zoomBox, zoomValues[zoomIndex], isInterpolating);
                            }else{
                                ZoomProcessor.paintZoomedImageWithShift(zoomedImage, zoomBox, zoomValues[zoomIndex],currentShift, isInterpolating);
                            }

                            zoomedImageIcon.setImage(zoomedImage);
                            zoomedImagePane.setViewportView(zoomedImageLabel);
                            zoomedImagePane.updateUI();
                        }
                    }
                }
            }
        });
        fileLoadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileOpen = new JFileChooser();
                int ret = fileOpen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileOpen.getSelectedFile();
                    if (file.getName().matches(".+\\.mbv")) {
                        loadedFileNameLabel.setText(file.getName());
                        try {
                            /*Считываем байты из массива*/
                            byte[] fileData = IOUtils.
                                    toByteArray(new FileInputStream(file.getAbsoluteFile().toString()));
                            /*Создаём матрицу яркостей из массива байтов*/
                            lightnessMatrix = createMatrix(fileData);
                            isFileLoaded = true;
                            /*Задаём высоту и ширину будущей картинки*/
                            imageHeight = lightnessMatrix.length;
                            imageWidth = lightnessMatrix[0].length;
                            /*Создаём объект картинки, которую потом будем перерисовывать*/
                            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
                            drawImageWithShift(currentShift);
                            /*отображение картинки в интерфейсе*/

                            ZoomProcessor.paintOverviewImage(overviewImage, lightnessMatrix);
                            showImageOnUI();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                            StringBuilder exceptionMessage = new StringBuilder();
                            StackTraceElement[] stackTrace = exception.getStackTrace();
                            for (StackTraceElement element : stackTrace) {
                                exceptionMessage.append(element).append("\n");
                            }
                            showMessageDialog(null, "Ошибка!!!\n" + exceptionMessage);
                        }
                    } else {
                        showMessageDialog(null, "Ошибка - неверный формат файла");
                    }
                }
            }
        });
        a0RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShift = 0;
                if (isFileLoaded && wasChosenBeforeButton != a0RadioButton) {
                    drawImageWithShift(currentShift);
                    showImageOnUI();
                }
                wasChosenBeforeButton = a0RadioButton;
            }
        });
        a1RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShift = 1;
                if (isFileLoaded && wasChosenBeforeButton != a1RadioButton) {
                    drawImageWithShift(currentShift);
                    showImageOnUI();
                }
                wasChosenBeforeButton = a1RadioButton;
            }
        });
        a2RadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentShift = 2;
                if (isFileLoaded && wasChosenBeforeButton != a2RadioButton) {
                    drawImageWithShift(currentShift);
                    showImageOnUI();
                }
                wasChosenBeforeButton = a2RadioButton;
            }
        });

        incrementZoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(zoomIndex < zoomValues.length - 1){
                    zoomIndex++;
                    zoomValueLabel.setText(String.valueOf(zoomValues[zoomIndex]));
                    zoomBoxSize = ZoomProcessor.ZOOMED_IMAGE_SIZE/zoomValues[zoomIndex];
                    zoomBox = new int[zoomBoxSize][zoomBoxSize];
                    zoomedImage = new BufferedImage(zoomBoxSize * zoomValues[zoomIndex], zoomBoxSize * zoomValues[zoomIndex], BufferedImage.TYPE_3BYTE_BGR );
                }
            }
        });
        decrementZoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (zoomIndex > 0) {
                    zoomIndex--;
                    zoomValueLabel.setText(String.valueOf(zoomValues[zoomIndex]));
                    zoomBoxSize = ZoomProcessor.ZOOMED_IMAGE_SIZE/zoomValues[zoomIndex];
                    zoomBox = new int[zoomBoxSize][zoomBoxSize];
                    zoomedImage = new BufferedImage(zoomBoxSize * zoomValues[zoomIndex], zoomBoxSize * zoomValues[zoomIndex], BufferedImage.TYPE_3BYTE_BGR );
                }
            }
        });
        normRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isNormalisation = normRadioButton.isSelected();
            }
        });
        interpolRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isInterpolating = interpolRadioButton.isSelected();
            }
        });
    }

    private void showImageOnUI() {
        imageIcon.setImage(image);
        imageScrollPane.setViewportView(imageLabel);
        overviewImageIcon.setImage(overviewImage);
        overviewImagePane.setViewportView(overviewImageLabel);
        imageScrollPane.updateUI();
        overviewImagePane.updateUI();
    }

    private void drawImageWithShift(int shift) {
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                image.setRGB(x, y, calculateRGB(lightnessMatrix[y][x], shift));
            }
        }
    }


    private void drawImageWithAutoMode() {
        /*обозначаем текущий диапазон значений*/
        int currentRangeMin = 0;//минимум
        int currentRangeMax = 1023;//максимум
        /*значение разности текущего максимума диапазона
        и текущего минимума диапазона*/
        int maxCurrentRangeDiffMinCurrentRange = currentRangeMax - currentRangeMin;
        /*обзначением новый максимум*/
        int newRangeMax = 255;
        /*Значение разности нового максимума и текущего минимума*/
        int newRangeMaxDiffCurrentRangeMin = newRangeMax - currentRangeMin;
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                //Рассчитываем яркость по формуле
                int lightness = (newRangeMaxDiffCurrentRangeMin * lightnessMatrix[y][x])
                        / maxCurrentRangeDiffMinCurrentRange + currentRangeMin;
                int rgb = (0b11111111 << 24) | (lightness << 16) | (lightness << 8) | lightness;
                image.setRGB(x,y,rgb);
            }
        }

    }

    public static void main(String[] args) throws Exception {
        GUI gui = new GUI();
        gui.setContentPane(gui.mainPanel);
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        gui.setMinimumSize(new Dimension(
                Toolkit.getDefaultToolkit().getScreenSize().width,
                Toolkit.getDefaultToolkit().getScreenSize().height - 50
        ));
        gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gui.setTitle("ПООВД");
        gui.setVisible(true);
    }

}
