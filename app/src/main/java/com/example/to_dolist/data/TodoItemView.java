package com.example.to_dolist.data;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.to_dolist.R;
import com.example.to_dolist.alarm.AlarmScheduler;

public class TodoItemView {

    private final Context context;
    private final TodoItem item;
    private final TodoItemDataSource dataSource;
    private final Runnable refreshCallback;
    private final View todoItemView;

    public TodoItemView(Context context, TodoItem item, TodoItemDataSource dataSource, Runnable refreshCallback) {
        this.context = context;
        this.item = item;
        this.dataSource = dataSource;
        this.refreshCallback = refreshCallback;

        LayoutInflater inflater = LayoutInflater.from(context);
        this.todoItemView = inflater.inflate(R.layout.todo_item_layout, null, false);

        initializeView(todoItemView);
    }

    private void initializeView(View view) {

        TextView todoText = view.findViewById(R.id.todo_text);
        CheckBox todoCheckbox = view.findViewById(R.id.todo_checkbox);
        ImageButton editButton = view.findViewById(R.id.edit_button);
        ImageButton deleteButton = view.findViewById(R.id.delete_button);

        // 제목
        todoText.setText(item.getTitle());
        todoCheckbox.setChecked(item.isCompleted());

        // 체크 상태 반영
        todoCheckbox.setText(item.isCompleted() ? "✓" : "");

        todoCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            todoCheckbox.setText(isChecked ? "✓" : "");
            item.setCompleted(isChecked);
            dataSource.updateTask(item);

            if (isChecked) {
                AlarmScheduler.cancelAlarm(context, item.getId());
                Toast.makeText(context, "완료됨: 알람 취소됨", Toast.LENGTH_SHORT).show();
            } else {
                if (item.getDueTime() != null) {
                    AlarmScheduler.scheduleAlarm(context, item);
                    Toast.makeText(context, "미완료: 알람 재설정됨", Toast.LENGTH_SHORT).show();
                }
            }

            refreshCallback.run();
        });

        deleteButton.setOnClickListener(v -> showDeleteDialog());
        editButton.setOnClickListener(v -> showEditDialog(todoText));
    }

    private void showEditDialog(TextView todoTextView) {
        Context safeContext = todoTextView.getRootView().getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(safeContext);
        builder.setTitle("할 일 수정");

        final EditText editText = new EditText(safeContext);
        editText.setText(item.getTitle());
        builder.setView(editText);

        builder.setPositiveButton("저장", (dialog, which) -> {
            String newTitle = editText.getText().toString().trim();
            if (newTitle.isEmpty()) return;

            item.setTitle(newTitle);
            dataSource.updateTask(item);
            todoTextView.setText(newTitle);

            refreshCallback.run();
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(context)
                .setTitle("삭제")
                .setMessage("'" + item.getTitle() + "' 삭제할까요?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    AlarmScheduler.cancelAlarm(context, item.getId());
                    dataSource.deleteTask(item.getId());
                    refreshCallback.run();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    public View getView() {
        return todoItemView;
    }
}
