/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioviz;

import static java.lang.Integer.min;
import java.util.ArrayList;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Arc;

/**
 *
 * @author James
 * used this youtube video for some guidance on making the game
 * https://www.youtube.com/watch?v=FVo1fm52hz0 
 * 
 */
public class Jccf9cSpaceInvader implements Visualizer{

    private final String name = "Space Invaders";
    private String vizPaneInitialStyle = "";

        
    private Integer numOfBands;
    private AnchorPane vizPane;
    
    private final Double bandHeightPercentage = 1.3;
    private final Double rectangleLength = 5.0;  // 10.0
    
    private Double width = 0.0;
    private Double height = 0.0;
    
    private Double bandWidth = 0.0;
    private Double bandHeight = 0.0;
    private Double halfBandHeight = 0.0;
    
    private Enemy[] enemies;
    private Double[] enemiesPosition;
    private Player player;
    private ArrayList<Projectile> projectiles;
    
    public Jccf9cSpaceInvader() {
        
    }
    
    public class Enemy extends Rectangle {
        private boolean dead = false;
        
        public Enemy(int i) {
            this.setX(bandWidth / 2 + bandWidth * i);
            this.setY(0);
            this.setWidth(bandWidth / 2);
            this.setHeight(bandWidth / 2);
            this.setFill(Color.RED);
        }
        
        public void shoot() {
            Projectile projectile = new Projectile(this);
            projectiles.add(projectile);
            vizPane.getChildren().add(projectile);
        }
    }
    
    public class Player extends Rectangle {
        
        public void moveLeft() {
            if (this.getX() > 2) {
                this.setX(this.getX() - 3);
            }
        }
        
        public void moveRight() {
            if (this.getX()+this.getWidth() < vizPane.getWidth() -2) {
                this.setX(this.getX() + 3);
            }
        }
        
        public void shoot() {
            Projectile projectile = new Projectile(this);
            projectiles.add(projectile);
            vizPane.getChildren().add(projectile);
        }
    }
    
    public class Projectile extends Rectangle {
        private final Rectangle who;
        
        public Projectile(Rectangle who) {
            this.who = who;
            this.setWidth(rectangleLength/2);
            this.setHeight(rectangleLength*2.5);
            this.setFill(Color.YELLOW);
            this.setY(who.getY());
            this.setX(who.getX() + who.getWidth()/2);
        }
        
        public void move() {
            if (this.who.equals(player)) {
                this.setY(this.getY() - 3);
            }
            else {
                this.setY(this.getY() + 3);
            }
        }    
    }
    
    @Override
    public void start(Integer numBands, AnchorPane vizPane) {
        end();
        vizPaneInitialStyle = vizPane.getStyle();
        vizPane.setStyle("-fx-background-color: black" );
        this.numOfBands = numBands;
        this.vizPane = vizPane;
        
        height = vizPane.getHeight();
        width = vizPane.getWidth();
        
        bandWidth = width / numBands;
        bandHeight = height * bandHeightPercentage;
        halfBandHeight = bandHeight / 2;
        enemies = new Enemy[numBands];
        enemiesPosition = new Double[numBands];
        projectiles = new ArrayList();
        
        
        for (int i = 0; i < numBands; i++) {
            Enemy enemy = new Enemy(i);
            vizPane.getChildren().add(enemy);
            enemies[i] = enemy;
            enemiesPosition[i] = enemy.getX();
        }    
        
        player = new Player();
            player.setWidth(rectangleLength*2);
            player.setHeight(player.getWidth());
            player.setFill(Color.GREEN);
            player.setX(vizPane.getWidth()/2);
            player.setY(vizPane.getHeight() - 10);
            
        vizPane.getChildren().add(player);
    }

    @Override
    public void end() {
        cleanUpProjectiles();
         if (enemies != null) {
             for (Enemy enemy : enemies) {
                vizPane.getChildren().remove(enemy);
             }
            vizPane.setStyle(vizPaneInitialStyle);
            enemies = null;
         }
        if (player != null) {
            vizPane.getChildren().remove(player);
        }
    }    

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void draw(double timestamp, double length, float[] magnitudes, float[] phases) {
            if (enemies == null) {
                return;
            }
                   
        Integer num = min(enemies.length, magnitudes.length);
        
        for (int i = 0; i < num; i++) {
            enemies[i].setHeight(rectangleLength + Math.abs((magnitudes[i]+70)/2));
            enemies[i].setWidth(enemies[i].getHeight());
            enemies[i].setX(enemiesPosition[i] - enemies[i].getWidth()/2 );
            if (enemies[i].getHeight() > rectangleLength + 15) {
                if (!enemies[i].dead) {
                    if (i < .1*num && enemies[i].getHeight() > rectangleLength + 22) {
                        enemies[i].shoot();
                    }
                    else if (i >.1*num) {
                        enemies[i].shoot();
                    }
                }
            }
        }
        
        
        //Handle projectile collisions and borders
        for (int i = 0; i < projectiles.size(); i++) {
            projectiles.get(i).move();
            if (projectiles.get(i).getY() < -5 ) {
                killProjectile(i);
            }
            if (projectiles.get(i).getY() > vizPane.getHeight() - 10 ) {
                killProjectile(i);
            }
            
            for (int j = 0; j < num; j++) {
                if (projectiles.get(i).getBoundsInParent().intersects(enemies[j].getBoundsInParent())) {
                    if (projectiles.get(i).who.equals(player)) {
                        killProjectile(i);
                        enemies[j].dead = true;
                        enemies[j].setY(-20);
                        vizPane.getChildren().remove(enemies[j]);
                    }
                }
            }
            if (projectiles.get(i).getBoundsInParent().intersects(player.getBoundsInParent())) {
                if (!projectiles.get(i).who.equals(player)) {
                    start(this.numOfBands, this.vizPane);
                }
            }
        }
    }
          
    public void controlPlayer(KeyCode press) {
        switch (press) {
            case A:
                player.moveLeft();
                break;
            case D:
                player.moveRight();
                break;
            case W:
                player.shoot();
                break;
        }
    }
        
    //method to clean up projectiles that get left over when bands or media is changed mid-song
    public void cleanUpProjectiles() {
        if (projectiles != null) {
            for (int i = 0; i < projectiles.size(); i++) {
                vizPane.getChildren().remove(projectiles.get(i));
                projectiles.set(i,null);
            }
            projectiles = null;
        }
    }
    
    public void killProjectile(int i) {
        vizPane.getChildren().remove(projectiles.get(i));
        projectiles.set(i, null);
        projectiles.remove(i);  
    }
}
