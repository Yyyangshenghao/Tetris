package com.example.tetris;

public class Tetromino {
    private int[][] shape;
    private int[][] previousShape;
    private int x, y;

    public Tetromino(int[][] shape) {
        this.shape = shape;
        this.x = 0;
        this.y = 0;
    }

    public int[][] getShape() {
        return shape;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void moveUp(){
        y--;
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public void rotate() {
        // 保存当前形状
        saveCurrentShape();
        int n = shape.length;
        int[][] rotatedShape = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rotatedShape[i][j] = shape[n - j - 1][i];
            }
        }
        shape = rotatedShape;
    }

    public void rotateBack() {
        // 恢复之前保存的形状
        if (previousShape != null) {
            shape = previousShape;
            previousShape = null;
        }
    }

    private void saveCurrentShape() {
        int n = shape.length;
        previousShape = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(shape[i], 0, previousShape[i], 0, n);
        }
    }

    // 获取方块的宽度
    public int getWidth() {
        return shape[0].length;
    }

    // 获取方块的高度
    public int getHeight() {
        return shape.length;
    }

    // 获取方块最左边的位置
    public int getLeftBound() {
        int minX = Integer.MAX_VALUE;
        for (int[] row : shape) {
            for (int j = 0; j < row.length; j++) {
                if (row[j] != 0) {
                    minX = Math.min(minX, x + j);
                }
            }
        }
        return minX;
    }

    // 获取方块最右边的位置
    public int getRightBound() {
        int maxX = Integer.MIN_VALUE;
        for (int[] row : shape) {
            for (int j = 0; j < row.length; j++) {
                if (row[j] != 0) {
                    maxX = Math.max(maxX, x + j);
                }
            }
        }
        return maxX;
    }
}
