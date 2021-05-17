package com.Oleuua;
import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.imageio.ImageIO; // обеспечивает простые операции загрузки и сохранения изображения
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import javax.swing.*;
import javax.swing.SwingWorker; // облегчает процесс организации фонового потока

public class FractalExplorer
{
    private int displaySize;
    private JImageDisplay image;
    private FractalGenerator gen;
    private Rectangle2D.Double range;
    private JComboBox<String> fractalChooser;
    private JButton saveBut;
    private JButton resetBut;
    private int rowsRemaining;

    private void enableUI(boolean val) // включение или выключение кнопки с выпадающим списком
    {
        fractalChooser.setEnabled(val);
        saveBut.setEnabled(val);
        resetBut.setEnabled(val);
    }

    public FractalExplorer(int size)
    {
        displaySize = size;
        gen = new Mandelbrot(); // создаем объект класса Мандельброта через ссылку баз.класса
        range = new Rectangle2D.Double();
        gen.getInitialRange(range);
    }

    public void createAndShowGUI()
    {
        JFrame frame = new JFrame("Фрактальный Исследователь"); // класс для создания окна графа приложения
        image = new JImageDisplay(displaySize, displaySize);
        resetBut = new JButton("Сброс дисплея");
        resetBut.setActionCommand("reset");
        FractalHandler handler = new FractalHandler();
        resetBut.addActionListener(handler);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// при закрытии окна закрывается и приложение
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(image, BorderLayout.CENTER); // размещение изображения по центру
        frame.getContentPane().add(resetBut, BorderLayout.SOUTH);// размещение кнопки ресет в южной части окна
        JPanel buttonsPanel = new JPanel();
        JPanel fractalPanel = new JPanel();

        JLabel label = new JLabel("Фрактал: ");
        fractalPanel.add(label);

        fractalChooser = new JComboBox<String>();
        fractalChooser.addItem(Mandelbrot.getString()); // добавление ячеек выбора
        fractalChooser.addItem(Tricorn.getString());
        fractalChooser.addItem(BurningShip.getString());
        fractalChooser.addActionListener(handler);

        fractalPanel.add(fractalChooser);

        frame.getContentPane().add(fractalPanel, BorderLayout.NORTH);

        buttonsPanel.add(resetBut);
        saveBut = new JButton("Сохранить изображение");
        saveBut.setActionCommand("save");
        saveBut.addActionListener(handler);
        buttonsPanel.add(saveBut);
        frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        frame.getContentPane().addMouseListener(new MouseHandler());// регистрируем экземпляр обработчика Mouse Listener в фрейме, метод извлекает слои панели содержимого для добавления объекта


        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private class FractalHandler implements ActionListener { // обработчик событий кнопок и поддержка списка фракталов
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (e.getSource() == fractalChooser) {
                String selectedItem = fractalChooser.getSelectedItem().toString(); // выбор из трех фракталов

                if (selectedItem.equals(Mandelbrot.getString())) {
                    gen = new Mandelbrot();
                } else if (selectedItem.equals(Tricorn.getString())) {
                    gen = new Tricorn();
                } else if (selectedItem.equals(BurningShip.getString())) {
                    gen = new BurningShip();
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка: Невозможно распознать выбор");
                    return;
                }

                range = new Rectangle2D.Double();
                gen.getInitialRange(range);
                drawFractal();

            } else if (cmd.equals("reset")) {
                image.clearImage();
                image.repaint();
                range = new Rectangle2D.Double();
                gen.getInitialRange(range);
                drawFractal();
                System.out.println("ok!");
            } else if (cmd.equals("save")) {
                JFileChooser chooser = new JFileChooser();

                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
                chooser.setFileFilter(filter);
                chooser.setAcceptAllFileFilterUsed(false); //строка гарантирует, что средство выбора не разрешит пользователю использование отличных от png форматов.


                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) { // открывает
                    // диалоговое окно «Сохранить файл», позволяя тем самым пользователю выбрать
                    // директорию для сохранения
                    try {
                        File f = chooser.getSelectedFile(); //Если пользователь уже выбрал директорию для сохранения
                        String filePath = f.getPath();
                        if (!filePath.toLowerCase().endsWith(".png")) {
                            f = new File(filePath + ".png");
                        }

                        ImageIO.write(image.getImage(), "png", f);
                    } catch (IOException exc) {
                        JOptionPane.showMessageDialog(null, "Ошибка: Не удалось сохранить изображение( "
                                + exc.getMessage() + " )");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка: Не удалось распознать действие");
            }
        }
    }

    private class MouseHandler extends MouseAdapter // обработчик клика мыши
    {

        public void mouseClicked(MouseEvent e)
        {
            System.out.println(rowsRemaining);
            if (rowsRemaining > 0)
                return;


            double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width,
                    displaySize, e.getX()); // x - пиксельная координата; xCoord - координата в пространстве фрактала

            double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height,
                    displaySize, e.getY());

            gen.recenterAndZoomRange(range, xCoord, yCoord, 0.5);

            drawFractal();

        }
    }

    private class FractalWorker extends SwingWorker<Object, Object> // SwingWorker - абстрактный класс, включающий методы doInBackground() и done()
    { // FractalWorker отвечает за вычисление значений цвета для  одной строки фрактала

        private int _y; // целочисленная y-координата вычисляемой строки

        private int[] _RGBVals; // массив чисел типа int для хранения вычисленных значений RGB для каждого пикселя в этой строке

        public FractalWorker(int y)
        {
            _y = y;
        }

        public Object doInBackground() // метод, который  выполняет фоновые операции
        {
            _RGBVals = new int[displaySize];

            double yCoord = FractalGenerator.getCoord(range.y, range.y + range.height,
                    displaySize, _y);

            for (int x = 0; x < displaySize; x++) { // цикл должен сохраняет каждое значение RGB в соответствующем элементе целочисленного массива

                double xCoord = FractalGenerator.getCoord(range.x, range.x + range.width,
                        displaySize, x);
                int numIters;
                int rgbColor = 0;
                float hue;

                numIters = gen.numIterations(xCoord, yCoord);
                if (numIters >= 0) {
                    hue = 0.7f + numIters / 200f;
                    rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                }

                _RGBVals[x] = rgbColor;
            }

            return null; // возвращает объект типа Object
        }

        public void done() // метод вызывается, когда фоновая задача завершена. Методу разрешено взаимодействовать с пользовательским интерфейсом
        {
            for (int x = 0; x < displaySize; x++) {
                image.drawPixel(x, _y, _RGBVals[x]);
            }

            image.repaint(0, 0, _y, displaySize, 1); // позволяет указать область для перерисовки
            System.out.println(rowsRemaining);
            if (rowsRemaining-- < 1) {
                enableUI(true); // все кнопки активны
            }
        }
    }

    public void drawFractal() // перерисовка фрактала
    {
        enableUI(false); // все кнопки запрещены

        for (int y = 0; y < displaySize; y++)
        {
            FractalWorker worker = new FractalWorker(y);
            worker.execute(); // запуск задачи в фоновом режиме
        }

        image.repaint();

    }
    public static void main(String[] args)
    {
        FractalExplorer explorer = new FractalExplorer(600); // размер вызываемого окна программы
        explorer.createAndShowGUI(); // отражение программы
        explorer.drawFractal(); // зарисовка фрактала
    }
}

