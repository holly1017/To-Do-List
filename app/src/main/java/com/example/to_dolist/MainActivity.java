package com.example.to_dolist;

import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.to_dolist.alarm.AlarmScheduler;
import com.example.to_dolist.data.TodoItem;
import com.example.to_dolist.data.TodoItemDataSource;
import com.example.to_dolist.data.TodoItemView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // 데이터 및 UI 요소
    private TodoItemDataSource dataSource;
    private LinearLayout todoListContainer;
    private EditText todoInputField;
    private EditText dateInputField;
    private Spinner categorySpinner;
    private CheckBox filterUncompleted;

    // 필터 TextView
    private TextView filterAll, filterWork, filterStudy, filterEtc;
    private final int COLOR_SELECTED = Color.parseColor("#4285F4");
    private final int COLOR_UNSELECTED = Color.parseColor("#E0E0E0");

    // 현재 필터 상태
    private String currentCategoryFilter = "ALL";
    private boolean isUncompletedFilterActive = false;

    // 앱 초기화 완료 상태 플래그
    private boolean isAppInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 데이터베이스 초기화 (아직 open은 하지 않음)
        dataSource = new TodoItemDataSource(this);

        // UI 요소 초기화 (onResume 전에 findViewById를 해야 setupApp에서 NullPointerException 방지)
        todoListContainer = findViewById(R.id.todo_list_container);
        todoInputField = findViewById(R.id.todo_input_field);
        dateInputField = findViewById(R.id.date_input_field);
        categorySpinner = findViewById(R.id.input_category_spinner);
        filterUncompleted = findViewById(R.id.filter_uncompeleted);

        initializeFiltersUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onResume 시마다 DB를 열고 권한을 확인하여 앱을 설정합니다.
        dataSource.open();
        checkAndSetupApp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 일시 중지 시 DB 연결을 닫습니다.
        dataSource.close();
    }

    /**
     * 알람 권한을 확인하고, 권한이 있다면 앱의 주요 기능을 초기화합니다.
     */
    private void checkAndSetupApp() {
        boolean canSchedule = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms()) {
                canSchedule = false;

                // **권한 요청**
                Toast.makeText(this, "정확한 알람을 위해 권한 설정이 필요합니다.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);

                Log.w(TAG, "정확한 알람 권한이 없어 설정 요청.");
            }
        }

        // 권한이 있거나, API 31 미만이며 초기화되지 않은 경우에만 설정 로직 실행
        if (canSchedule && !isAppInitialized) {
            setupAppFunctionality();
            isAppInitialized = true;
        } else if (canSchedule) {
            // 권한이 있고 이미 초기화된 경우, 목록만 새로고침 (onResume 재진입 시)
            applyFilters();
        } else {
            // 권한이 없는 경우, 알람/추가 버튼 비활성화 등의 처리 (선택 사항)
            Toast.makeText(this, "알람 기능 사용 불가: 권한을 허용해 주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 권한 확인 후 실행되는 앱의 주요 초기화 로직입니다.
     */
    private void setupAppFunctionality() {
        // 1. 시간 선택 기능 초기화
        ImageButton datePickerButton = findViewById(R.id.date_picker_button);
        datePickerButton.setOnClickListener(v -> showTimePickerDialog());

        // 2. 상단 필터링 및 카테고리 스피너 초기화
        initializeCategorySpinner();

        // 미완료 필터 체크박스 리스너
        filterUncompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isUncompletedFilterActive = isChecked;
            applyFilters();
        });

        // 3. 투두 추가 버튼 리스너
        ImageButton addTodoButton = findViewById(R.id.add_todo_button);
        addTodoButton.setOnClickListener(v -> addNewTodoItem());

        // 초기 데이터 로드 (필터링 적용)
        applyFilters();
        Log.d(TAG, "앱 주요 기능 초기화 완료.");
    }

    // onCreate에서 필터 UI만 미리 초기화
    private void initializeFiltersUI() {
        filterAll = findViewById(R.id.filter_all);
        filterWork = findViewById(R.id.filter_work);
        filterStudy = findViewById(R.id.filter_study);
        filterEtc = findViewById(R.id.filter_etc);

        // 기본 상태: 'ALL' 선택
        if (filterAll != null) {
            filterAll.setBackgroundColor(COLOR_SELECTED);
            filterAll.setOnClickListener(createFilterClickListener("ALL"));
        }

        if (filterWork != null) filterWork.setOnClickListener(createFilterClickListener("WORK"));
        if (filterStudy != null) filterStudy.setOnClickListener(createFilterClickListener("STUDY"));
        if (filterEtc != null) filterEtc.setOnClickListener(createFilterClickListener("ETC"));
    }

    private View.OnClickListener createFilterClickListener(final String category) {
        return v -> {
            resetFilterColors();
            v.setBackgroundColor(COLOR_SELECTED);
            currentCategoryFilter = category;
            applyFilters();
        };
    }

    private void resetFilterColors() {
        if (filterAll != null) filterAll.setBackgroundColor(COLOR_UNSELECTED);
        if (filterWork != null) filterWork.setBackgroundColor(COLOR_UNSELECTED);
        if (filterStudy != null) filterStudy.setBackgroundColor(COLOR_UNSELECTED);
        if (filterEtc != null) filterEtc.setBackgroundColor(COLOR_UNSELECTED);
    }

    private void initializeCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.category_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    /**
     * TimePickerDialog를 표시하여 마감 시간을 설정합니다.
     */
    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                MainActivity.this,
                (view, hourOfDay, selectedMinute) -> {
                    String timePart = String.format(Locale.KOREA, "%02d:%02d", hourOfDay, selectedMinute);
                    dateInputField.setText(timePart);
                },
                hour,
                minute,
                true // 24시간 형식
        );
        timePickerDialog.show();
    }

    /**
     * 필터 상태에 따라 투두 목록을 로드하고 UI를 업데이트합니다.
     */
    private void applyFilters() {
        List<TodoItem> items;

        if (currentCategoryFilter.equals("ALL")) {
            if (isUncompletedFilterActive) {
                items = dataSource.getTasksByCompletionStatus(false);
            } else {
                items = dataSource.getAllTasksSortedByTime();
            }
        } else {
            if (isUncompletedFilterActive) {
                List<TodoItem> allCategoryItems = dataSource.getTasksByCategory(currentCategoryFilter);
                items = new java.util.ArrayList<>();
                for (TodoItem item : allCategoryItems) {
                    if (!item.isCompleted()) {
                        items.add(item);
                    }
                }
            } else {
                items = dataSource.getTasksByCategory(currentCategoryFilter);
            }
        }

        updateTodoListUI(items);
        Log.d(TAG, "필터 적용 완료. 카테고리: " + currentCategoryFilter + ", 미완료 필터: " + isUncompletedFilterActive + ", 항목 수: " + items.size());
    }

    /**
     * 새 투두 항목을 생성하고 데이터베이스에 추가한 후 UI를 업데이트합니다.
     */
    private void addNewTodoItem() {
        String title = todoInputField.getText().toString().trim();
        String dueTime = dateInputField.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString().toUpperCase(Locale.ROOT);

        if (title.isEmpty()) {
            Toast.makeText(this, "할 일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        TodoItem newItem = new TodoItem(title, category, dueTime.isEmpty() ? null : dueTime, false);

        // 1. DB에 저장
        long id = dataSource.createTask(newItem);
        newItem.setId(id);

        // 2. 알람 스케줄링 (dueTime이 있을 경우)
        if (newItem.getDueTime() != null) {
            // 알람 권한 확인 후 스케줄링 시도
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms()) {
                AlarmScheduler.scheduleAlarm(this, newItem);
            } else {
                Toast.makeText(this, "알람 권한이 없어 알람 설정은 생략됩니다.", Toast.LENGTH_LONG).show();
            }
        }

        // 3. UI 업데이트 및 입력 필드 초기화
        todoInputField.setText("");
        dateInputField.setText("");
        categorySpinner.setSelection(0);

        Toast.makeText(this, "새 투두 항목이 추가되었습니다!", Toast.LENGTH_SHORT).show();
        applyFilters();
    }

    /**
     * 투두 목록 UI를 업데이트합니다.
     */
    private void updateTodoListUI(List<TodoItem> items) {
        todoListContainer.removeAllViews();

        if (items.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("할 일 목록이 비어있습니다.");
            emptyView.setTextColor(Color.DKGRAY);
            emptyView.setTextSize(16);
            emptyView.setPadding(16, 32, 16, 32);
            todoListContainer.addView(emptyView);
            return;
        }

        for (TodoItem item : items) {
            TodoItemView itemView = new TodoItemView(
                    this,
                    item,
                    dataSource,
                    this::applyFilters // 항목 변경 시 목록을 새로고침하는 콜백
            );
            todoListContainer.addView(itemView.getView());
        }
    }
}