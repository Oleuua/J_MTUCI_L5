package com.Oleuua;
import javax.swing.JComponent;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Graphics;

public class JImageDisplay extends JComponent{ // базовый компонент для всех граф. компонентов
    private BufferedImage img; // класс используется для обработки и управления данными изображениями
    public JImageDisplay (int w, int h) // ширина, высота
    {
        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB); // тип изображения, цвета
        setPreferredSize(new Dimension(w, h)); // класс изменерний содержит ширину и высоту объекта
    }
    protected void paintComponent(Graphics g) // вызывается при прорисовке компонента
    {
        super.paintComponent(g); // ссылка на базовый класс
        g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(),null); // рисует изображение в компоненте
    }
    public void drawPixel (int x, int y, int rgbColor) // устанавливает пиксель в определенный цвет
    {
        img.setRGB(x,y,rgbColor);
    }
    public  void clearImage() // метод устанавливает все пиксели изображения в черный цвет
    {
        for (int i=0; i<img.getWidth(); i++)
        {
            for (int j = 0; j < img.getHeight(); j++)
            {
                drawPixel(i, j, 0);
            }
        }
    }
    public BufferedImage getImage() {
        return img;
    }
}