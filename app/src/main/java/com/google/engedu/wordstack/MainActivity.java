/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;
    private Stack<LetterTile> placeTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                //Checking only 5 letter word go in the array list
                if(word.length() == WORD_LENGTH)
                    words.add(word);
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
//        if(word1LinearLayout != null)
//            word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());

        View word2LinearLayout = findViewById(R.id.word2);
//        if(word2LinearLayout != null)
//            word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());

        placeTiles = new Stack<>();
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placeTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) stackedLayout.peek();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }
                    placeTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        ViewGroup word1LinearLayout = (ViewGroup)findViewById(R.id.word1);
        ViewGroup word2LinearLayout = (ViewGroup)findViewById(R.id.word2);

        //Every time you click start just clear out the views
        //Getting the linearlayouts and removing all the views
        //Also clearing the stackLayout which contains all the letters
        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();
        try {
            stackedLayout.clear();
        } catch(EmptyStackException e){}

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        int index1 = random.nextInt(words.size());
        int index2;
        //we are looping through so that index1 and index2 are not same and that we dont end up
        //getting the same words i.e word1 and word2
        do {
            index2 = random.nextInt(words.size());
        }while(index2 == index1);

        word1 = words.get(index1);
        word2 = words.get(index2);

        String scrambledWord = "";
        int word1Count = 0;
        int word2Count = 0;
        while(word1Count < WORD_LENGTH || word2Count < WORD_LENGTH){
            //random.nextInt(2) to select either the first one or second word
            if(random.nextInt(2) == 1 && word1Count < WORD_LENGTH) {
                scrambledWord += word1.charAt(word1Count);
                word1Count++;
            }
            else if (word2Count < WORD_LENGTH) {
                scrambledWord += word2.charAt(word2Count);
                word2Count++;
            }

        }
        messageBox.setText(scrambledWord);

        for(int i = scrambledWord.length()-1; i >= 0; --i){
            stackedLayout.push(new LetterTile(this, scrambledWord.charAt(i)));
        }

        return true;
    }

    public boolean onUndo(View view) {

        if (!placeTiles.isEmpty()) {
            LetterTile tile = placeTiles.pop();
            tile.moveToViewGroup((ViewGroup) stackedLayout);
        }
        return true;
    }
}
