/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.epsilony.simpmeshfree.utils;

import java.awt.image.BufferedImage;

/**
 *
 * @author epsilon
 */
public interface DomainSelectListener {

    /**
     * 非自动的清除选择过程中的前一祯橡皮条效果
     * <br>当{@link #isRubberAutoClear() isRubberAutoClear()} 返回值为false时，此接口函数将不被调用
     * <br>需要在实现中调用{@link net.epsilony.simpmeshfree.utils.ModelPanelManager#getPanel() manager.getPanel()}.repaint(...);</br>
     * @param rubberImage
     * @param manager
     */
    public void clearRubber(BufferedImage rubberImage, ModelPanelManager manager);

    /**
     *
     * @return <br>true:不调用clearRubber并且自动将上一回调用{@link #selecting(int, int, int, int, net.epsilony.simpmeshfree.utils.ModelPanelManager, java.awt.image.BufferedImage) selecting(x1,y1,x2,y2,rubberImage,manager)}的屏幕上范围(x1,y1)-(x2,y2)内的橡皮条效果擦除</br>
     *         <br>false:当{@link #selecting(int, int, int, int, net.epsilony.simpmeshfree.utils.ModelPanelManager, java.awt.image.BufferedImage) selecting(x1,y1,x2,y2,rubberImage,manager)}的实现中对rubberImage的修改超出了范围(x1,y1)-(x2,y2)时返回false，并且必须实现{@link #clearRubber(java.awt.image.BufferedImage, net.epsilony.simpmeshfree.utils.ModelPanelManager) }</br>
     */
    public boolean isRubberAutoClear();

    /**
     *
     * @param x1 屏幕上第一点x坐标，不一定比第二点x坐标值小。
     * @param y1 屏幕上第一点y坐标，不一定比第二点x坐标值小。
     * @param x2 屏幕上第二点x坐标，不一定比第一点x坐标值大。
     * @param y2 屏幕上第二点y坐标，不一定比第一点x坐标值大。
     * @param manager
     * @param rubberImage
     */
    public void selecting(int x1, int y1, int x2, int y2, ModelPanelManager manager, BufferedImage rubberImage);

    /**<p>确定所选范围后被调用</p>
     * <br>一般利用屏幕空间所选的范围对角线坐标(x1,y1),(x2,y2)和manager.inverseTransform()获得模型空间对应的模型坐标</br>
     * <br>本接口一般要与{@link net.epsilony.simpmeshfree.utils.ModelImageWriter ModelImageWriter}联用并在本函数内更改ModelImageWriter运行所需数据</br>
     * @param x1 屏幕上第一点x坐标，不一定比第二点x坐标值小。
     * @param y1 屏幕上第一点y坐标，不一定比第二点x坐标值小。
     * @param x2 屏幕上第二点x坐标，不一定比第一点x坐标值大。
     * @param y2 屏幕上第二点y坐标，不一定比第一点x坐标值大。
     * @param manager {@link ModelPanelManager}
     * @return 是否要求重画JPanel 如返回值为false则一般情竞下需要本函数的实现中调用manager.repaintPanel()
     */
    public boolean selected(int x1, int y1, int x2, int y2, ModelPanelManager manager);
}
