package com.example.tetris;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class TetrisGame implements SurfaceHolder.Callback {
    private static final int ROWS = 20;
    private static final int COLUMNS = 10;
    private static final int BLOCK_SIZE = 55;
    private static final int[][][] SHAPES = {
            // S形
            {{0, 1, 1}, {1, 1, 0}, {0, 0, 0}},
            // Z形
            {{1, 1, 0}, {0, 1, 1}, {0, 0, 0}},
            // L形
            {{1, 0, 0}, {1, 0, 0}, {1, 1, 0}},
            // I形
            {{0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}},
            // O形
            {{1, 1}, {1, 1}},
            // T形
            {{0, 1, 0}, {1, 1, 1}, {0, 0, 0}}
    };

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView scoreTextView;
    private int[][] grid;
    private Tetromino currentTetromino;
    private int score;
    private Paint paint;
    private boolean isRunning;

    public TetrisGame(Context context, SurfaceView surfaceView, TextView scoreTextView) {
        this.surfaceView = surfaceView;
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.scoreTextView = scoreTextView;
        this.grid = new int[ROWS][COLUMNS];
        this.score = 0;
        this.paint = new Paint();
        this.isRunning = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        spawnTetromino();
        draw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 可以根据需要处理Surface的变化
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 可以根据需要处理Surface的销毁
    }

    private void spawnTetromino() {
        int shapeIndex = (int) (Math.random() * SHAPES.length);
        currentTetromino = new Tetromino(SHAPES[shapeIndex]);
        currentTetromino.setX(COLUMNS / 2 - currentTetromino.getShape()[0].length / 2);
        currentTetromino.setY(0);
    }

    public void handleShake(float x) {
        if (isRunning) {
            if (x > 2) {
                moveRight();
            } else if (x < -2) {
                moveLeft();
            }
            draw();
        }
    }

    public void moveRight() {
        if (isRunning) {
            currentTetromino.moveRight();
            if (checkCollision()) {
                currentTetromino.moveLeft();
            }
        }
    }

    public void moveLeft() {
        if (isRunning) {
            currentTetromino.moveLeft();
            if (checkCollision()) {
                currentTetromino.moveRight();
            }
        }
    }

    private boolean checkCollision() {
        int[][] shape = currentTetromino.getShape();
        int shapeX = currentTetromino.getX();
        int shapeY = currentTetromino.getY();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = shapeX + j;
                    int y = shapeY + i;
                    if (x < 0 || x >= COLUMNS || y >= ROWS || grid[y][x] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void update() {
        if (isRunning) {
            currentTetromino.moveDown();
            if (checkCollision()) {
                currentTetromino.moveUp();
                mergeTetromino();
                clearLines();
                spawnTetromino();
                if (checkCollision()) {
                    gameOver(); // 游戏结束处理逻辑
                }
            }
            draw();
        }
    }

    private void mergeTetromino() {
        int[][] shape = currentTetromino.getShape();
        int shapeX = currentTetromino.getX();
        int shapeY = currentTetromino.getY();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    grid[shapeY + i][shapeX + j] = shape[i][j];
                }
            }
        }
    }

    private void clearLines() {
        for (int i = 0; i < ROWS; i++) {
            boolean fullLine = true;
            for (int j = 0; j < COLUMNS; j++) {
                if (grid[i][j] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                for (int k = i; k > 0; k--) {
                    for (int l = 0; l < COLUMNS; l++) {
                        grid[k][l] = grid[k - 1][l];
                    }
                }
                for (int l = 0; l < COLUMNS; l++) {
                    grid[0][l] = 0;
                }
                score += 10;
                scoreTextView.setText("Score: " + score);
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.DKGRAY);

            // 获取SurfaceView的宽高
            int surfaceWidth = canvas.getWidth();
            int surfaceHeight = canvas.getHeight();

            // 计算网格的宽高
            int gridWidth = COLUMNS * BLOCK_SIZE;
            int gridHeight = ROWS * BLOCK_SIZE;

            // 计算偏移量，使网格居中
            int offsetX = (surfaceWidth - gridWidth) / 2;
            int offsetY = (surfaceHeight - gridHeight) / 2;

            // 绘制网格
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(1);
            for (int i = 0; i <= ROWS; i++) {
                float y = offsetY + i * BLOCK_SIZE;
                canvas.drawLine(offsetX, y, offsetX + gridWidth, y, paint);
            }
            for (int i = 0; i <= COLUMNS; i++) {
                float x = offsetX + i * BLOCK_SIZE;
                canvas.drawLine(x, offsetY, x, offsetY + gridHeight, paint);
            }

            // 绘制固定方块
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (grid[i][j] != 0) {
                        canvas.drawRect(offsetX + j * BLOCK_SIZE, offsetY + i * BLOCK_SIZE, offsetX + (j + 1) * BLOCK_SIZE, offsetY + (i + 1) * BLOCK_SIZE, paint);
                    }
                }
            }

            // 绘制当前方块
            int[][] shape = currentTetromino.getShape();
            int shapeX = currentTetromino.getX();
            int shapeY = currentTetromino.getY();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        canvas.drawRect(offsetX + (shapeX + j) * BLOCK_SIZE, offsetY + (shapeY + i) * BLOCK_SIZE, offsetX + (shapeX + j + 1) * BLOCK_SIZE, offsetY + (shapeY + i + 1) * BLOCK_SIZE, paint);
                    }
                }
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void gameOver() {
        isRunning = false;
        new Handler(Looper.getMainLooper()).post(() -> {
            new AlertDialog.Builder(surfaceView.getContext())
                    .setTitle("游戏结束！")
                    .setMessage("你的分数: " + score)
                    .setPositiveButton("重新开始", (dialog, which) -> {
                        resetGame();
                    })
                    .setNegativeButton("返回主菜单", (dialog, which) -> {
                        ((Activity) surfaceView.getContext()).finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void resetGame() {
        grid = new int[ROWS][COLUMNS];
        score = 0;
        scoreTextView.setText("Score: " + score);
        isRunning = true;
        spawnTetromino();
        draw();
    }

    public void rotate() {
        if (isRunning) {
            currentTetromino.rotate();
            while (currentTetromino.getX() < 0) {
                currentTetromino.moveRight();
            }
            while (currentTetromino.getX() + currentTetromino.getWidth() > COLUMNS) {
                currentTetromino.moveLeft();
            }
            if (checkCollision()) {
                // 还原旋转前的状态
                currentTetromino.rotateBack();
            }
        }
    }

    public void fastDrop() {
        if (isRunning) {
            while (!checkCollision()) {
                currentTetromino.moveDown();
            }
            currentTetromino.moveUp();
            mergeTetromino();
            clearLines();
            spawnTetromino();
            if (checkCollision()) {
                gameOver(); // 游戏结束处理逻辑
            }
            draw();
        }
    }
}
