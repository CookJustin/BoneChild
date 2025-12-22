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
        
        // Create a default procedural map - sized to match the screen
        // At 16px per tile: 80 tiles wide = 1280px, 45 tiles high = 720px
        generateMap(80, 45); // Matches typical 1280x720 screen
    }
    
    /**
     * Generate a procedural dungeon map with specified tiles
     */
    private void generateMap(int width, int height) {
        this.mapWidth = width;
        this.mapHeight = height;
        this.map = new int[height][width];
        
        // Define tile indices based on (column, row) positions
        // Border tiles: tile_1_0, tile_3_0, tile_2_0
        int[] borderTiles = {1, 3, 2}; // column positions in row 0
        
        // Interior tiles: tile_2_1, tile_2_2, tile_1_1, tile_3_1
        int[] interiorTiles = {
            tilesetColumns * 1 + 2,  // tile_2_1 (col 2, row 1)
            tilesetColumns * 2 + 2,  // tile_2_2 (col 2, row 2)
            tilesetColumns * 1 + 1,  // tile_1_1 (col 1, row 1)
            tilesetColumns * 1 + 3   // tile_3_1 (col 3, row 1)
        };
        
        // Fill the map
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tile;
                
                // Use border tiles for the edges
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    // Randomly pick from border tiles for variety
                    tile = borderTiles[(int)(Math.random() * borderTiles.length)];
                } else {
                    // Interior: randomly pick from interior tiles
                    tile = interiorTiles[(int)(Math.random() * interiorTiles.length)];
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
