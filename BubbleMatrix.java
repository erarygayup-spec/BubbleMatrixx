import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Properties;
import java.io.*;

public class BubbleMatrix extends JPanel implements ActionListener, KeyListener {
    // Game Settings
    private int width;
    private int height;
    private int lives = 10;
    private int score = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private long startTime;
    private boolean isGameOver = false;
    private boolean gameStarted = false;
    private boolean isPaused = false;
    private JButton resumeButton;
    private JButton pauseRestartButton;
    private JButton pauseExitButton;
    private String mode = "EASY"; // EASY, MODERATE, HARD, BOSS, SURVIVAL
    private int bossPhase = 1;
    private int highScore = 0;
    private long highTime = 0;

    // Components
    private Timer gameTimer;
    private List<Bubble> bubbles = new ArrayList<>();
    private List<MatrixRain> matrixRain = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private Boss boss;
    private JTextField inputField;
    private JButton startButton;
    private JButton difficultyButton;
    private JButton restartButton;
    private JButton easyButton;
    private JButton moderateButton;
    private JButton bossButton;
    private JButton survivalButton;
    private Random random = new Random();
    private StringBuilder currentInput = new StringBuilder();
    private long lastComboReset = 0;
    private boolean hasShield = false;
    private int achievements = 0;

    // Word Banks
    private String[] shortWords = {"cat", "dog", "sun", "code", "java", "link", "run", "jump", "play", "fish", "bird", "tree", "book", "pen", "car", "bus", "hat", "shoe", "ball", "game"};
    private String[] longWords = {"algorithm", "polymorphism", "framework", "interface", "abstract", "inheritance", "encapsulation", "compilation", "debugging", "optimization", "recursion", "iteration", "variable", "function", "method", "class", "object", "array", "list", "map"};

    public BubbleMatrix() {
        // Get screen dimensions for full screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.width = screenSize.width;
        this.height = screenSize.height;
        
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(10, 25, 40)); // Dark Matrix Blue
        this.setLayout(null);
        this.setFocusable(true);
        this.addKeyListener(this);
        loadRecords();

        // Initialize matrix rain background
        for (int i = 0; i < 120; i++) {
            matrixRain.add(new MatrixRain(random.nextInt(width), random.nextInt(height / 2), random.nextInt(2) + 2));
        }

        startButton = new JButton("START GAME");
        startButton.setBounds((width - 240) / 2, (height - 200), 240, 60);
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.setBackground(new Color(0, 170, 255));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> startGame());
        this.add(startButton);

        difficultyButton = new JButton("SET DIFFICULTY");
        difficultyButton.setBounds((width - 240) / 2, (height - 120), 240, 50);
        difficultyButton.setFont(new Font("Arial", Font.BOLD, 20));
        difficultyButton.setBackground(new Color(80, 80, 80));
        difficultyButton.setForeground(Color.WHITE);
        difficultyButton.setFocusPainted(false);
        difficultyButton.addActionListener(e -> {
            startButton.setVisible(false);
            difficultyButton.setVisible(false);
            easyButton.setVisible(true);
            moderateButton.setVisible(true);
            bossButton.setVisible(true);
            survivalButton.setVisible(true);
            repaint();
        });
        this.add(difficultyButton);

        restartButton = new JButton("RESTART GAME");
        restartButton.setBounds((width - 240) / 2, (height - 160), 240, 60);

        restartButton.setFont(new Font("Arial", Font.BOLD, 24));
        restartButton.setBackground(new Color(255, 100, 100));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());
        this.add(restartButton);

        easyButton = new JButton("EASY");
        easyButton.setBounds((width - 1000) / 2, (height - 300), 200, 50);
        easyButton.setFont(new Font("Arial", Font.BOLD, 20));
        easyButton.setBackground(new Color(0, 170, 255));
        easyButton.setForeground(Color.WHITE);
        easyButton.setFocusPainted(false);
        easyButton.setVisible(false);
        easyButton.addActionListener(e -> {
            mode = "EASY";
            lives = 15;
            hideDifficultyButtons();
        });
        this.add(easyButton);

        moderateButton = new JButton("MODERATE");
        moderateButton.setBounds((width - 1000) / 2 + 220, (height - 300), 200, 50);
        moderateButton.setFont(new Font("Arial", Font.BOLD, 20));
        moderateButton.setBackground(new Color(255, 165, 0));
        moderateButton.setForeground(Color.WHITE);
        moderateButton.setFocusPainted(false);
        moderateButton.setVisible(false);
        moderateButton.addActionListener(e -> {
            mode = "MODERATE";
            lives = 10;
            hideDifficultyButtons();
        });
        this.add(moderateButton);

        bossButton = new JButton("BOSS MODE");
        bossButton.setBounds((width - 1000) / 2 + 440, (height - 300), 200, 50);
        bossButton.setFont(new Font("Arial", Font.BOLD, 20));
        bossButton.setBackground(Color.RED);
        bossButton.setForeground(Color.WHITE);
        bossButton.setFocusPainted(false);
        bossButton.setVisible(false);
        bossButton.addActionListener(e -> {
            mode = "BOSS";
            lives = 15;
            hideDifficultyButtons();
        });
        this.add(bossButton);

        survivalButton = new JButton("SURVIVAL");
        survivalButton.setBounds((width - 1000) / 2 + 660, (height - 300), 200, 50);
        survivalButton.setFont(new Font("Arial", Font.BOLD, 20));
        survivalButton.setBackground(Color.GREEN);
        survivalButton.setForeground(Color.WHITE);
        survivalButton.setFocusPainted(false);
        survivalButton.setVisible(false);
        survivalButton.addActionListener(e -> {
            mode = "SURVIVAL";
            lives = 10;
            hideDifficultyButtons();
        });
        this.add(survivalButton);

        resumeButton = new JButton("RESUME");
        resumeButton.setBounds((width - 200) / 2, height / 2 - 50, 200, 50);
        resumeButton.setFont(new Font("Arial", Font.BOLD, 20));
        resumeButton.setBackground(new Color(0, 170, 255));
        resumeButton.setForeground(Color.WHITE);
        resumeButton.setFocusPainted(false);
        resumeButton.setVisible(false);
        resumeButton.addActionListener(e -> togglePause());
        this.add(resumeButton);

        pauseRestartButton = new JButton("RESTART");
        pauseRestartButton.setBounds((width - 200) / 2, height / 2 + 20, 200, 50);
        pauseRestartButton.setFont(new Font("Arial", Font.BOLD, 20));
        pauseRestartButton.setBackground(new Color(255, 165, 0));
        pauseRestartButton.setForeground(Color.WHITE);
        pauseRestartButton.setFocusPainted(false);
        pauseRestartButton.setVisible(false);
        pauseRestartButton.addActionListener(e -> {
            togglePause();
            restartCurrentGame();
        });
        this.add(pauseRestartButton);

        pauseExitButton = new JButton("EXIT TO MENU");
        pauseExitButton.setBounds((width - 200) / 2, height / 2 + 90, 200, 50);
        pauseExitButton.setFont(new Font("Arial", Font.BOLD, 20));
        pauseExitButton.setBackground(new Color(255, 50, 50));
        pauseExitButton.setForeground(Color.WHITE);
        pauseExitButton.setFocusPainted(false);
        pauseExitButton.setVisible(false);
        pauseExitButton.addActionListener(e -> {
            togglePause();
            exitGame();
        });
        this.add(pauseExitButton);

        inputField = new JTextField();
        inputField.setBounds((width - 260) / 2, height - 100, 260, 30);
        inputField.setVisible(false);
        inputField.setEnabled(false);
        this.add(inputField);

        startTime = System.currentTimeMillis();
        gameTimer = new Timer(20, this); // ~50 FPS
        gameTimer.start();
    }

    private void togglePause() {
        isPaused = !isPaused;
        resumeButton.setVisible(isPaused);
        pauseRestartButton.setVisible(isPaused);
        pauseExitButton.setVisible(isPaused);
        if (isPaused) {
            gameTimer.stop();
        } else {
            gameTimer.start();
        }
        repaint();
    }

    private void hideDifficultyButtons() {
        easyButton.setVisible(false);
        moderateButton.setVisible(false);
        bossButton.setVisible(false);
        survivalButton.setVisible(false);
        startButton.setVisible(true);
        difficultyButton.setVisible(true);
        repaint();
    }


    private void spawnBubble() {
        int speed = 2;
        String word = shortWords[random.nextInt(shortWords.length)];
        int bubbleType = 1;  // 1 = normal, 3 = shield, 4 = health regen

        // Difficulty Logic
        switch (mode) {
            case "EASY": speed = 1; break;
            case "MODERATE": speed = 3; break;
            case "HARD": speed = 5; break;
            case "BOSS": speed = 7; word = longWords[random.nextInt(longWords.length)]; break;
            case "SURVIVAL":
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                speed = 2 + (int)(elapsed / 30); // Speed up every 30s
                if (elapsed > 60) word = longWords[random.nextInt(longWords.length)];
                break;
        }
        
        // Special bubbles
        int specialChance = random.nextInt(100);
        if (specialChance < 15) bubbleType = 3; // 15% shield
        else if (mode.equals("SURVIVAL") && specialChance < 30) bubbleType = 4; // 15% health regen in Survival

        int bubbleWidth = Math.max(80, 20 + word.length() * 18);
        int bubbleHeight = 70 + Math.min(24, word.length() * 2);
        bubbleWidth = Math.min(bubbleWidth, width - 40);

        bubbles.add(new Bubble(random.nextInt(Math.max(1, width - bubbleWidth)), 0, word, speed, bubbleWidth, bubbleHeight, bubbleType));
    }

    private void spawnBoss() {
        if (boss == null && mode.equals("BOSS")) {
            boss = new Boss(width / 2, 80, "DEFEAT ME", 150, 100, bossPhase);
        }
    }

    private boolean checkWord(String typed) {
        // Check boss first in BOSS mode
        if (boss != null && boss.health > 0 && boss.word.equalsIgnoreCase(typed)) {
            boss.health -= 10;
            comboHit();
            playSound("boss_hit");
            createParticles(boss.x + boss.width / 2, boss.y + boss.height / 2, 30, new Color(255, 100, 0));
            if (boss.health <= 0) {
                score += 100 * (1 + combo);
                bossPhase++;
                if (bossPhase > 3) {
                    isGameOver = true;
                    achievements += 50;
                }
                boss = null;
            }
            currentInput.setLength(0);
            inputField.setText("");
            return true;
        }
        
        // Check regular bubbles
        for (int i = 0; i < bubbles.size(); i++) {
            if (bubbles.get(i).word.equalsIgnoreCase(typed)) {
                Bubble b = bubbles.get(i);
                createParticles(b.x + b.width / 2, b.y + b.height / 2, 25, new Color(0, 200, 255));
                playSound("pop");
                
                int baseScore = 10;
                if (b.type == 3) { baseScore = 0; hasShield = true; }  // Shield
                else if (b.type == 4) { baseScore = 20; lives = Math.min(lives + 1, 20); }  // Health regen
                
                score += baseScore * (1 + combo);
                comboHit();
                bubbles.remove(i);
                currentInput.setLength(0);
                inputField.setText("");
                return true;
            }
        }
        comboReset();
        return false;
    }
    private void comboHit() {
        combo++;
        if (combo > maxCombo) maxCombo = combo;
        lastComboReset = System.currentTimeMillis();
    }
    
    private void comboReset() {
        if (System.currentTimeMillis() - lastComboReset > 3000) {
            combo = 0;
        }
    }
    
    private void createParticles(int x, int y, int count, Color color) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, random.nextInt(6) - 3, random.nextInt(8) - 4, color));
        }
    }
    
    private void playSound(String soundType) {
        // Placeholder for sound system - can be extended with actual audio
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!gameStarted) {
            startButton.setVisible(true);
            difficultyButton.setVisible(true);
            restartButton.setVisible(false);
            drawHomepage(g2);
            return;
        }

        startButton.setVisible(false);
        difficultyButton.setVisible(false);
        restartButton.setVisible(isGameOver);

        // Draw animated background
        drawMatrixBackground(g2);

        // Draw UI
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Score: " + score, 30, 50);
        g2.setColor(Color.RED);
        g2.drawString("Lives: ", 30, 80);
        drawHearts(g2, lives, 150, 65);
        g2.setColor(Color.CYAN);
        g2.drawString("Mode: " + mode, 30, 110);
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        int nextY = 140;
        if (mode.equals("SURVIVAL")) {
            g2.setColor(Color.YELLOW);
            g2.drawString("Time: " + elapsed + "s", 30, 140);
            nextY = 170;
        }
        g2.setColor(Color.MAGENTA);
        g2.drawString("High Score: " + highScore + " (" + highTime + "s)", 30, nextY);

        // Draw current input letters centered below
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String currentLetters = currentInput.toString().toUpperCase();
        if (!currentLetters.isEmpty()) {
            int inputWidth = g2.getFontMetrics().stringWidth(currentLetters) + 40;
            int inputX = (width - inputWidth) / 2;
            int inputY = 140;
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRoundRect(inputX, inputY - 38, inputWidth, 54, 24, 24);
            g2.setColor(new Color(255, 240, 180));
            g2.drawString(currentLetters, (width - g2.getFontMetrics().stringWidth(currentLetters)) / 2, inputY);
        }

        // Draw combo and achievements
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(255, 150, 50));
        g2.drawString("Combo: " + combo + "x (Max: " + maxCombo + "x)", width - 380, 50);
        g2.setColor(new Color(100, 255, 100));
        g2.drawString("Achievements: " + achievements, width - 380, 80);
        
        if (hasShield) {
            g2.setColor(new Color(100, 200, 255));
            g2.drawString("⚡ SHIELD ACTIVE", width - 380, 110);
        }

        // Draw Particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.life <= 0) {
                particles.remove(i);
            } else {
                g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), p.life));
                g2.fillOval(p.x, p.y, 4, 4);
            }
        }

        // Draw Boss in BOSS mode
        if (boss != null && boss.health > 0) {
            drawBoss(g2, boss);
        }

        // Draw Bubbles
        for (Bubble b : bubbles) {
            int w = b.width;
            int h = b.height;
            GeneralPath bubbleShape = new GeneralPath();
            bubbleShape.moveTo(b.x + w * 0.2, b.y);
            bubbleShape.quadTo(b.x + w * 1.05, b.y + h * 0.18, b.x + w * 0.78, b.y + h * 0.32);
            bubbleShape.quadTo(b.x + w * 1.05, b.y + h * 0.65, b.x + w * 0.43, b.y + h);
            bubbleShape.quadTo(b.x - w * 0.10, b.y + h * 0.62, b.x + w * 0.2, b.y);
            bubbleShape.closePath();

            Paint originalPaint = g2.getPaint();
            
            // Different colors based on bubble type
            Color startColor = new Color(0, 180, 255, 180);
            Color endColor = new Color(20, 40, 120, 120);
            if (b.type == 3) { startColor = new Color(100, 255, 100, 180); endColor = new Color(50, 150, 50, 120); }  // Shield
            else if (b.type == 4) { startColor = new Color(255, 255, 100, 180); endColor = new Color(200, 200, 50, 120); }  // Health regen
            
            GradientPaint bubblePaint = new GradientPaint(b.x, b.y, startColor, b.x + w, b.y + h, endColor);
            g2.setPaint(bubblePaint);
            g2.fill(bubbleShape);
            
            // Glow effect
            g2.setColor(new Color(0, 255, 255, 80));
            g2.setStroke(new BasicStroke(4));
            g2.draw(bubbleShape);
            
            g2.setPaint(originalPaint);

            g2.setColor(new Color(255, 255, 255, 120));
            g2.fillOval(b.x + w/5, b.y + h/8, w/3, h/3);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.draw(bubbleShape);

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics bubbleFm = g2.getFontMetrics();
            int wordWidth = bubbleFm.stringWidth(b.word);
            int wordX = b.x + (w - wordWidth) / 2;
            int wordY = b.y + (h + bubbleFm.getAscent()) / 2 - 4;
            g2.drawString(b.word, wordX, wordY);
            
            // Icon for special bubbles
            if (b.type == 3) {
                g2.setColor(Color.GREEN);
                g2.drawString("ɨ", b.x + w - 15, b.y + 15);
            } else if (b.type == 4) {
                g2.setColor(Color.YELLOW);
                g2.drawString("♥", b.x + w - 15, b.y + 15);
            }
        }

        if (isGameOver) {
            g2.setColor(new Color(0,0,0,200));
            g2.fillRect(0,0, width, height);
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 80));
            g2.drawString("GAME OVER", (width - g2.getFontMetrics().stringWidth("GAME OVER")) / 2, height / 2 - 50);
            g2.setColor(new Color(255, 255, 100));
            g2.setFont(new Font("Arial", Font.BOLD, 36));
            g2.drawString("Final Score: " + score, (width - g2.getFontMetrics().stringWidth("Final Score: " + score)) / 2, height / 2 + 60);
        }

        if (isPaused) {
            g2.setColor(new Color(0,0,0,150));
            g2.fillRect(0,0, width, height);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            g2.drawString("PAUSED", (width - g2.getFontMetrics().stringWidth("PAUSED")) / 2, height / 2 - 100);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            g2.drawString("Press ESC to resume", (width - g2.getFontMetrics().stringWidth("Press ESC to resume")) / 2, height / 2 - 60);
        }
    }

    private void drawHearts(Graphics2D g2, int count, int startX, int startY) {
        int heartSize = 15;
        int spacing = 20;
        for (int i = 0; i < count; i++) {
            drawHeart(g2, startX + (i * spacing), startY, heartSize);
        }
    }

    private void drawHeart(Graphics2D g2, int x, int y, int size) {
        // Draw a proper heart shape
        GeneralPath heart = new GeneralPath();
        
        double w = size;
        double h = size;
        
        // Start at bottom point
        heart.moveTo(x, y + h * 0.4);
        
        // Left side curve to left bump
        heart.curveTo(x - w * 0.5, y - h * 0.3, x - w * 0.5, y - h * 0.1, x - w * 0.2, y + h * 0.2);
        
        // Left bump
        heart.curveTo(x - w * 0.4, y - h * 0.2, x - w * 0.25, y - h * 0.35, x, y - h * 0.15);
        
        // Right bump
        heart.curveTo(x + w * 0.25, y - h * 0.35, x + w * 0.4, y - h * 0.2, x + w * 0.2, y + h * 0.2);
        
        // Right side curve to bottom point
        heart.curveTo(x + w * 0.5, y - h * 0.1, x + w * 0.5, y - h * 0.3, x, y + h * 0.4);
        
        heart.closePath();
        
        g2.fill(heart);
    }

    private void drawMatrixBackground(Graphics2D g2) {
        // Update and draw matrix rain with better visibility
        for (MatrixRain m : matrixRain) {
            m.update(height);
            g2.setColor(new Color(0, 255, 100, 200));
            g2.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2.drawString(m.character, m.x, m.y);
        }
    }

    private void drawBoss(Graphics2D g2, Boss b) {
        int bw = b.width;
        int bh = b.height;
        int bx = b.x;
        int by = b.y;

        // Boss body - large hexagon shape with animation
        int[] xPoints = {bx + bw/2, bx + bw, bx + bw, bx + bw/2, bx, bx};
        int[] yPoints = {by, by + bh/3, by + 2*bh/3, by + bh, by + 2*bh/3, by + bh/3};
        
        // Boss coloring based on health
        float healthPercent = (float) b.health / (100f * b.phase);
        int red = (int) (255 * (1 - healthPercent));
        int green = (int) (100 * healthPercent);
        
        // Pulsing effect
        long pulse = System.currentTimeMillis() % 500;
        int extraSize = (int) (pulse > 250 ? 5 : 0);
        
        g2.setColor(new Color(255, red, 0, 200));
        g2.fillPolygon(xPoints, yPoints, 6);
        g2.setColor(new Color(255, 255, 0, 100 + extraSize * 10));
        g2.setStroke(new BasicStroke(4 + extraSize));
        g2.drawPolygon(xPoints, yPoints, 6);

        // Boss word label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        int wordWidth = g2.getFontMetrics().stringWidth(b.word);
        g2.drawString(b.word, bx + (bw - wordWidth) / 2, by + bh / 2 + 7);

        // Health bar with phase info
        int healthBarWidth = bw;
        int healthBarHeight = 25;
        int healthValue = Math.min(100, b.health);
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(bx, by - 40, healthBarWidth, healthBarHeight);
        g2.setColor(new Color(255, 100 + (int)(155 * healthPercent), 100));
        g2.fillRect(bx, by - 40, (int)(healthBarWidth * Math.min(1, (float)healthValue / 100)), healthBarHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(bx, by - 40, healthBarWidth, healthBarHeight);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("PHASE " + b.phase + " HP: " + Math.max(0, b.health) + "/" + (100 * b.phase), bx + 10, by - 18);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameStarted || isGameOver || isPaused) return;
        
        comboReset();

        // Spawn boss once at start of BOSS mode
        if (mode.equals("BOSS") && boss == null) {
            spawnBoss();
        }

        // Spawn logic
        if (random.nextInt(100) < (mode.equals("BOSS") ? 3 : 2)) {
            spawnBubble();
        }
        
        // Move bubbles
        for (int i = 0; i < bubbles.size(); i++) {
            Bubble b = bubbles.get(i);
            b.y += b.speed;

            if (b.y > height - 50) {
                createParticles(b.x + b.width / 2, b.y + b.height / 2, 20, new Color(255, 50, 50));
                if (!hasShield) {
                    lives--;
                    if (lives <= 0) isGameOver = true;
                } else {
                    hasShield = false;
                }
                bubbles.remove(i);
            }
        }

        // Survival Timer Check
        if (mode.equals("SURVIVAL")) {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            if (elapsed >= 300) isGameOver = true; // 5 Minute limit
        }

        if (isGameOver) {
            long gameTime = (System.currentTimeMillis() - startTime) / 1000;
            if (score > highScore || (score == highScore && gameTime > highTime)) {
                highScore = score;
                highTime = gameTime;
                saveRecords();
            }
        }

        repaint();
    }

    private void drawHomepage(Graphics2D g2) {
        // Background layers
        GradientPaint bg1 = new GradientPaint(0, 0, new Color(0, 8, 25), width, 0, new Color(15, 0, 40));
        g2.setPaint(bg1);
        g2.fillRect(0, 0, width, height);
        GradientPaint bg2 = new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, height, new Color(0, 40, 80, 120));
        g2.setPaint(bg2);
        g2.fillRect(0, 0, width, height);

        // Draw matrix rain background (subtle)
        for (MatrixRain m : matrixRain) {
            g2.setColor(new Color(0, 200, 100, 60));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.drawString(m.character, m.x, m.y);
        }

        // Floating spark lines
        g2.setColor(new Color(0, 200, 255, 80));
        for (int i = 0; i < 5; i++) {
            int y = 80 + i * 100;
            g2.drawLine(80, y, width - 80, y - 20);
        }

        // Centered title (higher up)
        g2.setFont(new Font("Arial", Font.BOLD, 72));
        g2.setColor(new Color(0, 255, 255, 220));
        String title = "BUBBLE MATRIX";
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (width - titleWidth) / 2, height / 5);

        // Subtitle
        g2.setFont(new Font("Arial", Font.ITALIC, 28));
        g2.setColor(new Color(150, 255, 180));
        String subtitle = "Typing enhanced word shooter";
        int subtitleWidth = fm.stringWidth(subtitle);
        g2.drawString(subtitle, (width - subtitleWidth) / 2, height / 5 + 60);

        // Info box (moved higher)
        int boxWidth = 520;
        int boxHeight = 140;
        int boxX = (width - boxWidth) / 2;
        int boxY = height / 3 + 20;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 30, 30);
        g2.setColor(new Color(0, 170, 255, 180));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 30, 30);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.WHITE);
        String[] lines = {
            "Type letters to build a word.",
            "When the word matches a bubble, it pops automatically.",
            "Choose difficulty, then press START to begin."
        };
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], boxX + 30, boxY + 40 + i * 30);
        }

        // Start button instruction (moved higher)
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(255, 220, 80));
        String buttonHint = "Press START, or press D to choose difficulty first.";
        g2.drawString(buttonHint, (width - g2.getFontMetrics().stringWidth(buttonHint)) / 2, boxY + boxHeight + 30);
    }

    private void startGame() {
        gameStarted = true;
        isGameOver = false;
        isPaused = false;
        currentInput.setLength(0);
        boss = null;
        bossPhase = 1;
        combo = 0;
        maxCombo = 0;
        achievements = 0;
        hasShield = false;
        startButton.setVisible(false);
        difficultyButton.setVisible(false);
        restartButton.setVisible(false);
        easyButton.setVisible(false);
        moderateButton.setVisible(false);
        bossButton.setVisible(false);
        survivalButton.setVisible(false);
        resumeButton.setVisible(false);
        pauseRestartButton.setVisible(false);
        pauseExitButton.setVisible(false);
        inputField.setVisible(false);
        inputField.setEnabled(false);
        startTime = System.currentTimeMillis();
        bubbles.clear();
        particles.clear();
        score = 0;
        lives = 10;
        if (mode.equals("BOSS")) lives = 15;
        this.requestFocusInWindow();
    }

    private void restartGame() {
        gameStarted = false;
        isGameOver = false;
        isPaused = false;
        currentInput.setLength(0);
        bubbles.clear();
        particles.clear();
        boss = null;
        score = 0;
        lives = 10;
        combo = 0;
        hasShield = false;
        startButton.setVisible(true);
        difficultyButton.setVisible(true);
        restartButton.setVisible(false);
        easyButton.setVisible(false);
        moderateButton.setVisible(false);
        bossButton.setVisible(false);
        survivalButton.setVisible(false);
        resumeButton.setVisible(false);
        pauseRestartButton.setVisible(false);
        pauseExitButton.setVisible(false);
        this.requestFocusInWindow();
        repaint();
    }

    private void restartCurrentGame() {
        isGameOver = false;
        isPaused = false;
        currentInput.setLength(0);
        boss = null;
        bossPhase = 1;
        combo = 0;
        maxCombo = 0;
        achievements = 0;
        hasShield = false;
        startTime = System.currentTimeMillis();
        bubbles.clear();
        particles.clear();
        score = 0;
        lives = 10;
        if (mode.equals("BOSS")) lives = 15;
        this.requestFocusInWindow();
    }

    private void exitGame() {
        gameStarted = false;
        isGameOver = false;
        isPaused = false;
        currentInput.setLength(0);
        bubbles.clear();
        particles.clear();
        boss = null;
        score = 0;
        lives = 10;
        combo = 0;
        hasShield = false;
        startButton.setVisible(true);
        difficultyButton.setVisible(true);
        restartButton.setVisible(false);
        easyButton.setVisible(false);
        moderateButton.setVisible(false);
        bossButton.setVisible(false);
        survivalButton.setVisible(false);
        resumeButton.setVisible(false);
        pauseRestartButton.setVisible(false);
        pauseExitButton.setVisible(false);
        this.requestFocusInWindow();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameStarted) {
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            togglePause();
            return;
        }

        if (isGameOver || isPaused) return;

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (checkWord(currentInput.toString())) {
                currentInput.setLength(0);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
        } else if (Character.isLetter(e.getKeyChar())) {
            currentInput.append(Character.toLowerCase(e.getKeyChar()));
            checkWord(currentInput.toString());
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private void loadRecords() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("records.properties")) {
            props.load(fis);
            highScore = Integer.parseInt(props.getProperty("highScore", "0"));
            highTime = Long.parseLong(props.getProperty("highTime", "0"));
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
            highTime = 0;
        }
    }

    private void saveRecords() {
        Properties props = new Properties();
        props.setProperty("highScore", String.valueOf(highScore));
        props.setProperty("highTime", String.valueOf(highTime));
        try (FileOutputStream fos = new FileOutputStream("records.properties")) {
            props.store(fos, "BubbleMatrix Records");
        } catch (IOException e) {
            // Ignore
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("BubbleMatrix - Typing Enhanced");
        BubbleMatrix game = new BubbleMatrix();
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false);
        frame.setVisible(true);
    }

    // Inner class for Bubble data
    class Bubble {
        int x, y, speed, width, height, type;
        String word;
        Bubble(int x, int y, String word, int speed, int width, int height, int type) {
            this.x = x; this.y = y; this.word = word; this.speed = speed;
            this.width = width; this.height = height; this.type = type;
        }
    }

    // Inner class for Boss with phases
    class Boss {
        int x, y, width, height, health, phase, maxHealth;
        String word;
        Boss(int x, int y, String word, int width, int height, int phase) {
            this.x = x; this.y = y; this.word = word; this.width = width; this.height = height;
            this.phase = phase;
            this.maxHealth = 100 * phase;
            this.health = this.maxHealth;
        }
    }

    // Inner class for Matrix Rain
    class MatrixRain {
        int x, y, speed;
        String character;
        MatrixRain(int x, int y, int speed) {
            this.x = x; this.y = y; this.speed = speed;
            this.character = String.valueOf((char)(random.nextInt(94) + 33));
        }
        void update(int panelHeight) {
            this.y += speed;
            if (this.y > panelHeight) {
                this.y = 0;
                this.character = String.valueOf((char)(random.nextInt(94) + 33));
            }
        }
    }

    // Inner class for Particle effects
    class Particle {
        int x, y;
        double vx, vy;
        Color color;
        int life;

        Particle(int x, int y, double vx, double vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = 255;
        }

        void update() {
            x += (int)vx;
            y += (int)vy;
            vy += 0.2; // Gravity
            life -= 8;
        }
    }
}