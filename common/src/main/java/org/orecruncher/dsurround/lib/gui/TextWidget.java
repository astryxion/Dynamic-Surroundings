package org.orecruncher.dsurround.lib.gui;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;

public class TextWidget extends AbstractStringWidget {

    public TextWidget(int x, int y, int width, int height, Component component, Font font) {
        super(x, y, width, height, component, font);
    }

    @Override
    public void visitLines(ActiveTextCollector collector) {
        int y = getY();

        int nameWidth = this.getFont().width(this.getMessage());
        if (nameWidth > getWidth()) {
            collector.acceptScrolling(this.getMessage(), getX(), y, getX() + getWidth(), y + this.getFont().lineHeight, -1);
        } else {
            collector.accept(getX(), y, this.getMessage());
        }
    }
}
