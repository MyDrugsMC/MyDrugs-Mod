package org.mydrugs.mydrugs.menu.layout;

public final class LayoutMath {
    private LayoutMath() {
    }

    public static int centered(int outerSize, int innerSize) {
        return (outerSize - innerSize) / 2;
    }

    public static int centeredAt(int start, int outerSize, int innerSize) {
        return start + centered(outerSize, innerSize);
    }

    public static void requireEven(String name, int value) {
        if ((value & 1) != 0) {
            throw new IllegalArgumentException(name + " must be even, got " + value);
        }
    }

    public static void requireFits(String name, int outerSize, int innerSize) {
        if (innerSize > outerSize) {
            throw new IllegalArgumentException(name + " innerSize " + innerSize + " > outerSize " + outerSize);
        }
    }

    public static int innerStart(int outerStart, int padding) {
        return outerStart + padding;
    }

    public static int slotCoord(int panelStart, int padding, int index, int slotSize) {
        return panelStart + padding + index * slotSize;
    }

    public static int horizontalSpread(int start, int totalWidth, int itemWidth, int count, int index) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("index out of bounds: " + index);
        }
        if (count == 1) {
            return start + (totalWidth - itemWidth) / 2;
        }

        int free = totalWidth - count * itemWidth;
        int gap = free / (count - 1);
        return start + index * (itemWidth + gap);
    }

    public static int verticalSpread(int start, int totalHeight, int itemHeight, int count, int index) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("index out of bounds: " + index);
        }
        if (count == 1) {
            return start + (totalHeight - itemHeight) / 2;
        }

        int free = totalHeight - count * itemHeight;
        int gap = free / (count - 1);
        return start + index * (itemHeight + gap);
    }

    public static int horizontalSpreadWithOuterGaps(int start, int totalWidth, int itemWidth, int count, int index) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("index out of bounds: " + index);
        }

        int free = totalWidth - count * itemWidth;
        int gap = free / (count + 1);
        return start + gap + index * (itemWidth + gap);
    }

    public static int verticalSpreadWithOuterGaps(int start, int totalHeight, int itemHeight, int count, int index) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("index out of bounds: " + index);
        }

        int free = totalHeight - count * itemHeight;
        int gap = free / (count + 1);
        return start + gap + index * (itemHeight + gap);
    }
}