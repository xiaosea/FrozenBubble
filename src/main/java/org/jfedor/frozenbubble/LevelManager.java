/*
 *                 [[ Frozen-Bubble ]]
 *
 * Copyright (c) 2000-2003 Guillaume Cottenceau.
 * Java sourcecode - Copyright (c) 2003 Glenn Sanson.
 * Additional source - Copyright (c) 2013 Eric Fortin.
 *
 * This code is distributed under the GNU General Public License
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 or 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to:
 * Free Software Foundation, Inc.
 * 675 Mass Ave
 * Cambridge, MA 02139, USA
 *
 * Artwork:
 *    Alexis Younes <73lab at free.fr>
 *      (everything but the bubbles)
 *    Amaury Amblard-Ladurantie <amaury at linuxfr.org>
 *      (the bubbles)
 *
 * Soundtrack:
 *    Matthias Le Bidan <matthias.le_bidan at caramail.com>
 *      (the three musics and all the sound effects)
 *
 * Design & Programming:
 *    Guillaume Cottenceau <guillaume.cottenceau at free.fr>
 *      (design and manage the project, whole Perl sourcecode)
 *
 * Java version:
 *    Glenn Sanson <glenn.sanson at free.fr>
 *      (whole Java sourcecode, including JIGA classes
 *             http://glenn.sanson.free.fr/jiga/)
 *
 * Android port:
 *    Pawel Aleksander Fedorynski <pfedor@fuw.edu.pl>
 *    Eric Fortin <videogameboy76 at yahoo.com>
 *    Copyright (c) Google Inc.
 *
 *          [[ http://glenn.sanson.free.fr/fb/ ]]
 *          [[ http://www.frozen-bubble.org/   ]]
 */

package org.jfedor.frozenbubble;

import android.os.Bundle;

import java.util.Random;
import java.util.Vector;

public class LevelManager {

  /*
   * Level row and column definitions.
   */
  public static final int LANES    = 15;
  public static final int MAX_ROWS = 15;
  public static final int NUM_COLS = 8;
  public static final int NUM_ROWS = 13;
  public static final int VS_ROWS  = 5;

  /*
   * Level difficulty (number of bubble colors) definitions.
   */
  public static final int EASY     = 4;
  public static final int NORMAL   = 5;
  public static final int MODERATE = 6;
  public static final int HARD     = 7;
  public static final int INSANE   = 8;

  public static final String[] DifficultyStrings = {
    "frozen bubble",
    "frozen bubble",
    "frozen bubble",
    "frozen bubble",
    "easy",
    "normal",
    "medium",
    "hard",
    "insane"
  };

  private boolean randomMode;
  private long randomSeed;
  private int  currentLevel;
  private int  rowOffset;
  private Vector<byte[][]> levelList;

  public void saveState(Bundle map) {
    map.putInt("LevelManager-currentLevel", currentLevel);
    map.putInt("LevelManager-rowOffset", rowOffset);
  }

  public void restoreState(Bundle map) {
    currentLevel = map.getInt("LevelManager-currentLevel");
    rowOffset = map.getInt("LevelManager-rowOffset");
  }

  /**
   * Constructor used to provide randomly generated levels.
   * @param seed - the random bubble generation seed.
   * @param difficulty - the number of different bubble colors to
   * generate.  Higher numbers make the level more difficult to play.
   * Use the static difficulty values defined in this class to set the
   * level difficulty, e.g. <code>EASY</code>, <code>HARD</code>, etc.
   */
  public LevelManager(long seed, int difficulty) {
    randomMode   = true;
    randomSeed   = seed;
    currentLevel = difficulty;
    if (FrozenBubble.arcadeGame) {
      rowOffset = 1;
    }
    else {
      rowOffset = 0;
    }
    if (currentLevel < EASY)
      currentLevel = EASY;
    else if (currentLevel > INSANE) {
      currentLevel = INSANE;
    }
    levelList = new Vector<byte[][]>();
    levelList.addElement(getLevel(null));
  }

  /**
   * Constructor used to parse levels provided via a formatted array.
   * @param levels - the byte array containing the level information.
   * @param startingLevel - the current level starting index.
   */
  public LevelManager(byte[] levels, int startingLevel) {
    randomMode = false;
    randomSeed = 0;
    String allLevels = new String(levels);
    currentLevel = startingLevel;
    rowOffset = 0;
    levelList = new Vector<byte[][]>();
    int nextLevel = allLevels.indexOf("\n\n");

    if (nextLevel == -1 && allLevels.trim().length() != 0) {
      nextLevel = allLevels.length();
    }

    while (nextLevel != -1) {
      String currentLevel = allLevels.substring(0, nextLevel).trim();
      levelList.addElement(getLevel(currentLevel));
      allLevels = allLevels.substring(nextLevel).trim();

      if (allLevels.length() == 0) {
        nextLevel = -1;
      }
      else {
        nextLevel = allLevels.indexOf("\n\n");

        if (nextLevel == -1) {
          nextLevel = allLevels.length();
        }
      }
    }

    if (currentLevel >= levelList.size()) {
      currentLevel = 0;
    }
  }

  private byte[][] getLevel(String data) {
    byte[][] temp = new byte[NUM_COLS][NUM_ROWS - 1];

    for (int j = 0; j < (NUM_ROWS - 1); j++) {
      for (int i = 0; i < NUM_COLS; i++) {
        temp[i][j] = -1;
      }
    }

    if (!randomMode) {
      int tempX = 0;
      int tempY = 0;

      for (int i=0 ; i<data.length() ; i++) {
        if (data.charAt(i) >= 48 && data.charAt(i) <= 55) {
          temp[tempX][tempY] = (byte)(data.charAt(i) - 48);
          tempX++;
        }
        else if (data.charAt(i) == 45) {
          temp[tempX][tempY] = -1;
          tempX++;
        }

        if (tempX == NUM_COLS) {
          tempY++;

          if (tempY == NUM_ROWS - 1) {
            return temp;
          }

          tempX = tempY % 2;
        }
      }
    }
    else {
      Random rand = new Random(randomSeed);
      for (int j = 0; j < VS_ROWS; j++) {
        for (int i = 0; i < NUM_COLS; i++) {
          temp[i][j] = (byte)rand.nextInt(currentLevel);
        }
        if (FrozenBubble.arcadeGame) {
          rowOffset = (rowOffset + 1) % 2;
        }
      }
      randomSeed = rand.nextInt();
    }
    return temp;
  }

  public byte[] getNewRow(BubbleSprite[][] grid) {
    byte[] tempRow = new byte[NUM_COLS];
    Random rand    = new Random(randomSeed);

    for (int column = 0; column < NUM_COLS; column++) {
      tempRow[column] = (byte)rand.nextInt(currentLevel);
    }

    if (FrozenBubble.arcadeGame) {
      rowOffset = (rowOffset + 1) % 2;
    }
    randomSeed = rand.nextInt();
    return tempRow;
  }

  public int getRowOffset() {
    return rowOffset;
  }

  public byte[][] getCurrentLevel() {
    if (!randomMode) {
      if (currentLevel < levelList.size()) {
        return (byte[][])levelList.elementAt(currentLevel);
      }
    }
    else {
      if (FrozenBubble.arcadeGame) {
        rowOffset = VS_ROWS % 2;
      }
      return (byte[][])levelList.elementAt(0);
    }
    return null;
  }

  public void goToNextLevel() {
    if (!randomMode) {
      currentLevel++;
      if (currentLevel >= levelList.size()) {
        currentLevel = 0;
      }
    }
    else {
      levelList.clear();
      levelList.addElement(getLevel(null));
    }
  }

  public void goToFirstLevel() {
    if (!randomMode) {
      currentLevel = 0;
    }
  }

  public int getLevelIndex() {
    return currentLevel;
  }
}
