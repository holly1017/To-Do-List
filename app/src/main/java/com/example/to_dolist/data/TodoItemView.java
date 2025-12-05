package com.example.to_dolist.data;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.to_dolist.R;
import com.example.to_dolist.alarm.AlarmScheduler;

/**
 * 투두 항목 하나를 표시하고 상호작용을 처리하는 클래스.
 * activity_main.xml에 정의된 아이템 레이아웃 구조를 활용합니다.
 */
public class TodoItemView {
    private final Context context;
    private final TodoItem item;
    private final TodoItemDataSource dataSource;
    private final Runnable refreshCallback; // 목록 새로고침 콜백
    private final View todoItemView;

    public TodoItemView(Context context, TodoItem item, TodoItemDataSource dataSource, Runnable refreshCallback) {
        this.context = context;
        this.item = item;
        this.dataSource = dataSource;
        this.refreshCallback = refreshCallback;

        // 아이템 레이아웃을 인플레이트 (ScrollView 내부에 있는 ConstraintLayout 구조)
        LayoutInflater inflater = LayoutInflater.from(context);
        this.todoItemView = inflater.inflate(R.layout.todo_item_layout, null, false); // 별도 레이아웃 파일이 없으므로, 메인 XML의 항목 구조를 복사해서 만듭니다. (일단은 임시로 null 사용)
        // **실제로는 재활용을 위해 별도의 todo_item.xml 레이아웃 파일이 필요합니다.
        // 현재는 activity_main.xml에 있는 첫 번째 항목 레이아웃을 재활용한다고 가정하고 ID를 사용합니다.**

        // activity_main.xml의 항목 레이아웃을 재활용한다고 가정하고 ID를 사용합니다.
        // 실무에서는 별도의 XML 파일을 사용하는 것이 일반적입니다.
        if (this.todoItemView instanceof androidx.constraintlayout.widget.ConstraintLayout) {
            // 메인 XML의 첫 번째 항목 ConstraintLayout을 복사하여 재구성했다고 가정
        }


        initializeView(todoItemView);
    }

    private void initializeView(View view) {
        // ID는 activity_main.xml의 투두 항목 구조를 따름
        TextView todoText = view.findViewById(R.id.todo_text);
        CheckBox todoCheckbox = view.findViewById(R.id.todo_checkbox);
        ImageButton editButton = view.findViewById(R.id.edit_button);
        ImageButton deleteButton = view.findViewById(R.id.delete_button);

        // 데이터 설정
        todoText.setText(item.getTitle());
        String categoryText = item.getCategoryId() + (item.getDueTime() != null ? " (" + item.getDueTime() + ")" : "");
        todoCheckbox.setChecked(item.isCompleted());
        // 완료된 항목은 텍스트에 취소선 효과를 줄 수 있음 (선택 사항)

        // 체크박스 리스너 (RQ-0005: 상태 변경)
        todoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setCompleted(isChecked);
            dataSource.updateTask(item);

            if (isChecked) {
                // 완료 시 알람 취소
                AlarmScheduler.cancelAlarm(context, (int) item.getId());
                Toast.makeText(context, "항목 완료됨. 알람 취소.", Toast.LENGTH_SHORT).show();
            } else if (item.getDueTime() != null) {
                // 미완료로 변경 시 알람 재등록
                AlarmScheduler.scheduleAlarm(context, item);
                Toast.makeText(context, "항목 미완료로 변경. 알람 재등록.", Toast.LENGTH_SHORT).show();
            }
            refreshCallback.run(); // 목록 새로고침
        });

        // 삭제 버튼 리스너 (RQ-0002)
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        // 수정 버튼 리스너 (RQ-0003: 구현의 복잡성으로 인해 단순 토스트 메시지로 대체)
        editButton.setOnClickListener(v -> Toast.makeText(context, item.getTitle() + " 항목 수정 기능 호출 (구현 필요)", Toast.LENGTH_SHORT).show());
        // 실제 구현 시, 새로운 Activity/Dialog를 열어 항목 수정 후 DB 업데이트 및 알람 재등록 필요
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(context)
                .setTitle("투두 삭제")
                .setMessage("'" + item.getTitle() + "' 항목을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    // 1. 알람 취소
                    AlarmScheduler.cancelAlarm(context, (int) item.getId());
                    // 2. DB에서 삭제
                    dataSource.deleteTask(item.getId());
                    // 3. UI 새로고침
                    Toast.makeText(context, "항목 삭제됨.", Toast.LENGTH_SHORT).show();
                    refreshCallback.run();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public View getView() {
        return todoItemView;
    }
}