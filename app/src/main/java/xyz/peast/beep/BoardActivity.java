package xyz.peast.beep;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class BoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Intent intent = getIntent();

        String boardName = intent.getStringExtra(MainActivity.BOARD_NAME_SELECTED);

        TextView textView = (TextView) findViewById(R.id.board_name_textview);

        textView.setText(boardName);
    }
}
