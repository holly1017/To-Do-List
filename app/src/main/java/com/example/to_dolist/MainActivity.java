package com.example.to_dolist;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // 필터 TextView들을 멤버 변수로 선언
    private TextView filterAll;
    private TextView filterWork;
    private TextView filterStudy;
    private TextView filterEtc;

    // 선택/미선택 색상 정의
    private final int COLOR_SELECTED = Color.parseColor("#4285F4"); // 파란색
    private final int COLOR_UNSELECTED = Color.parseColor("#E0E0E0"); // 회색

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =================================================================
        // 1. 시간 선택 기능 초기화
        // =================================================================
        final ImageButton datePickerButton = findViewById(R.id.date_picker_button);
        final EditText dateInputField = findViewById(R.id.date_input_field);
        final Calendar calendar = Calendar.getInstance();

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                                String timePart = String.format(Locale.KOREA, "%02d:%02d", hourOfDay, minute);
                                dateInputField.setText(timePart);
                            }
                        },
                        hour,
                        minute,
                        true
                );
                timePickerDialog.show();
            }
        });

        // =================================================================
        // 2. 상단 필터링 기능 및 색상 변경 초기화
        // =================================================================
        // XML에서 필터 TextView들을 ID로 찾습니다. (XML에 ID 추가 필수)
        filterAll = findViewById(R.id.filter_all);
        filterWork = findViewById(R.id.filter_work);
        filterStudy = findViewById(R.id.filter_study);
        filterEtc = findViewById(R.id.filter_etc);

        // 앱 시작 시 'ALL' 필터를 선택된 상태로 설정합니다.
        // XML에서 이미 #4285F4로 설정되어 있다면 이 코드는 주석 처리하거나,
        // XML에서 filter_all의 배경색을 COLOR_UNSELECTED로 변경 후 이 코드를 사용하세요.
        // if (filterAll != null) filterAll.setBackgroundColor(COLOR_SELECTED);

        // 클릭 리스너 정의 (색상 변경 및 필터링 로직 포함)
        View.OnClickListener filterClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = "";

                // 1. 모든 필터의 색상을 미선택 상태로 초기화합니다.
                resetFilterColors();

                // 2. 클릭된 필터의 색상을 선택된 색상으로 변경하고 카테고리를 설정합니다.
                v.setBackgroundColor(COLOR_SELECTED);

                int viewId = v.getId();

                if (viewId == R.id.filter_all) {
                    category = "ALL";
                } else if (viewId == R.id.filter_work) {
                    category = "WORK";
                } else if (viewId == R.id.filter_study) {
                    category = "STUDY";
                } else if (viewId == R.id.filter_etc) {
                    category = "ETC";
                }

                filterTodoList(category);
            }
        };

        // 각 필터 TextView에 리스너 연결 (null 체크)
        if (filterAll != null) filterAll.setOnClickListener(filterClickListener);
        if (filterWork != null) filterWork.setOnClickListener(filterClickListener);
        if (filterStudy != null) filterStudy.setOnClickListener(filterClickListener);
        if (filterEtc != null) filterEtc.setOnClickListener(filterClickListener);
    }

    /**
     * 모든 필터 버튼의 배경색을 미선택 색상(회색)으로 초기화합니다.
     */
    private void resetFilterColors() {
        // null 체크를 통해 findViewById에 실패했더라도 안전하게 처리합니다.
        if (filterAll != null) filterAll.setBackgroundColor(COLOR_UNSELECTED);
        if (filterWork != null) filterWork.setBackgroundColor(COLOR_UNSELECTED);
        if (filterStudy != null) filterStudy.setBackgroundColor(COLOR_UNSELECTED);
        if (filterEtc != null) filterEtc.setBackgroundColor(COLOR_UNSELECTED);
    }

    /**
     * 투두 리스트를 해당 카테고리로 필터링하는 메소드 (예시)
     */
    private void filterTodoList(String category) {
        Log.d(TAG, "필터링 요청됨: " + category + " (색상 변경 완료)");

        // TODO: 여기에 실제 데이터 로드 및 필터링 로직을 구현하세요.
    }
}