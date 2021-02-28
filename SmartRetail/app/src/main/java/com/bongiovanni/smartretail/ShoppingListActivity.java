package com.bongiovanni.smartretail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

public class ShoppingListActivity extends AppCompatActivity {

    private ListView ShoppingList;
    private EditText ItemEdit;
    private Button AddButton;
    private Button MainButton;
    private ArrayAdapter<String> Adapter;
    public Set<String> shoppingList = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        ShoppingList = (ListView) findViewById(R.id.shopping_listView);
        ItemEdit = (EditText) findViewById(R.id.item_editText);
        AddButton = (Button) findViewById(R.id.add_button);
        MainButton = (Button) findViewById(R.id.main_button);

        Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ShoppingList.setAdapter(Adapter);

        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = ItemEdit.getText().toString();
                shoppingList.add(item);
                Adapter.add(item);
                Adapter.notifyDataSetChanged();
                ItemEdit.setText("");
            }
        });

        MainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("sharedList", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putStringSet("shoppingList", shoppingList);
                editor.commit();
                Intent mainActivityIntent = new Intent(ShoppingListActivity.this, MainActivity.class);
                startActivity(mainActivityIntent);
            }
        });
    }


}
