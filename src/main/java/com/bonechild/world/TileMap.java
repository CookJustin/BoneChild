package com.bonechild.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Handles tile-based map rendering using a tileset
 */
public class TileMap {
    private Texture tileset;
    private TextureRegion[][] tiles;
    private int[][] map;
    
    private int tileSize = 32;
    private int mapWidth;
    private int mapHeight;
    private int tilesetColumns;
    private int tilesetRows;
    
    public TileMap(Texture tileset, int tileSize) {
        this.tileset = tileset;
        this.tileSize = tileSize;
        
        // Calculate tileset dimensions
        this.tilesetColumns = tileset.getWidth() / tileSize;
        this.tilesetRows = tileset.getHeight() / tileSize;
        
        // Split tileset into individual tiles
        tiles = new TextureRegion[tilesetRows][tilesetColumns];
        for (int row = 0; row < tilesetRows; row++) {
            for (int col = 0; col < tilesetColumns; col++) {
                tiles[row][col] = new TextureRegion(tileset, 
                    col * tileSize, row * tileSize, tileSize, tileSize);
            }
        }
        
        // Create a default procedural map
        generateMap(50, 50); // 50x50 tiles
    }
    
    /**
     * Generate a procedural graveyard map
     */
    private void generateMap(int width, int height) {
        this.mapWidth = width;
        this.mapHeight = height;
        this.map = new int[height][width];
        
        // Fill with grass tiles - using tile 0 (top-left, most plain) as border
        // and use other tiles sparsely in the interior
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tile;
                
                // Use tile 0 (top-left, most plain) for borders
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    tile = 0; // Border tile (plain)
                } else {
                    // Interior: mostly tile 0 with sparse decorative elements
                    double rand = Math.random();
                    
                    if (rand < 0.85) {
                        // Base tile for majority (85%)
                        tile = 0;
                    } else if (rand < 0.95) {
                        // Sparse decorative elements from row 1 (10%)
                        tile = tilesetColumns + (int)(Math.random() * Math.min(3, tilesetColumns));
                    } else {
                        // Rare special tiles (5%)
                        tile = (int)(Math.random() * Math.min(4, tilesetColumns));
                    }
                }
                
                map[y][x] = tile;
            }
        }
    }
    
    /**
     * Render the visible portion of the map
     */
    public void render(SpriteBatch batch, float camX, float camY, float viewportWidth, float viewportHeight) {
        // Calculate which tiles are visible
        int startX = Math.max(0, (int)(camX / tileSize) - 1);
        int endX = Math.min(mapWidth, (int)((camX + viewportWidth) / tileSize) + 2);
        int startY = Math.max(0, (int)(camY / tileSize) - 1);
        int endY = Math.min(mapHeight, (int)((camY + viewportHeight) / tileSize) + 2);
        
        batch.begin();
        
        // Render visible tiles
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tileIndex = map[y][x];
                int tileRow = tileIndex / tilesetColumns;
                int tileCol = tileIndex % tilesetColumns;
                
                // Make sure we don't go out of bounds
                if (tileRow < tilesetRows && tileCol < tilesetColumns) {
                    TextureRegion tile = tiles[tileRow][tileCol];
                    batch.draw(tile, x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
        
        batch.end();
    }
    
    /**
     * Get tile at world position
     */
    public int getTileAt(float worldX, float worldY) {
        int tileX = (int)(worldX / tileSize);
        int tileY = (int)(worldY / tileSize);
        
        if (tileX >= 0 && tileX < mapWidth && tileY >= 0 && tileY < mapHeight) {
            return map[tileY][tileX];
        }
        return -1;
    }
    
    public int getMapWidth() { return mapWidth * tileSize; }
    public int getMapHeight() { return mapHeight * tileSize; }
    public int getTileSize() { return tileSize; }
}
