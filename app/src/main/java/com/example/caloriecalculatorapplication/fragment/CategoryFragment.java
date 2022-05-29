package com.example.caloriecalculatorapplication.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.caloriecalculatorapplication.DBAdapter;
import com.example.caloriecalculatorapplication.MainActivity;
import com.example.caloriecalculatorapplication.R;
import com.example.caloriecalculatorapplication.SignUp;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    Cursor categoriesCursor;
    private View mainView;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //set title
        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Categories");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateList("0", "");

        //create menu
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        ((MainActivity)getActivity()).getMenuInflater().inflate(R.menu.menu_categories, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        Toast.makeText(getActivity(), "XXX" +menuItem, Toast.LENGTH_LONG).show();
        int id = menuItem.getItemId();
        if(id == R.id.action_add){
            createNewCategory();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @SuppressLint("Range")
    public void populateList(String parentID, String parentName){
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //get category
        String fields[] = new String[]{
                "_id",
                "category_name",
                "category_parent_id"
        };
        categoriesCursor = db.select("categories", fields, "category_parent_id", parentID);

        // Createa a array
        ArrayList<String> values = new ArrayList<String>();

        // Convert categories to string
        int categoriesCount = categoriesCursor.getCount();
        for(int x=0; x<categoriesCount; x++){
            values.add(categoriesCursor.getString(categoriesCursor.getColumnIndex("category_name")));
            categoriesCursor.moveToNext();
        }

        // Close cursor
        categoriesCursor.close();

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);

        // Set Adapter
        ListView lv = (ListView)getActivity().findViewById(R.id.lvCategories);
        lv.setAdapter(adapter);

        // OnClick
        if(parentID.equals("0")) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    listItemClicked(arg2);
                }
            });
        }

        db.close();
    }

    //List item clicked
    public void listItemClicked(int listItemIDClicked){
        // Move cursor to ID clicked
        categoriesCursor.moveToPosition(listItemIDClicked);

        // Get ID and name from cursor
        String id = categoriesCursor.getString(0);
        String name = categoriesCursor.getString(1);
        String parentID = categoriesCursor.getString(2);

        // Change title
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(name);

        // Move to sub class
        populateListSub(id, name);
    }

    @SuppressLint("Range")
    public void populateListSub(String parentID, String parentName){
        DBAdapter db = new DBAdapter(getActivity());
        db.open();

        //get category
        String fields[] = new String[]{
                "_id",
                "category_name",
                "category_parent_id"
        };
        Cursor subCursor = db.select("categories", fields, "category_parent_id", parentID);

        // Createa a array
        ArrayList<String> values = new ArrayList<String>();

        // Convert categories to string
        int cursorCount = subCursor.getCount();
        for(int x=0; x<cursorCount; x++){
            values.add(subCursor.getString(subCursor.getColumnIndex("category_name")));
            subCursor.moveToNext();
        }

        // Close cursor
        categoriesCursor.close();

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);

        // Set Adapter
        ListView lv = (ListView)getActivity().findViewById(R.id.lvCategories);
        lv.setAdapter(adapter);

        // OnClick
//        if(parentID.equals("0")) {
//            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                    listItemClicked(arg2);
//                }
//            });
//        }

        db.close();

    }

    public void createNewCategory(){
        int id = R.layout.fragment_categories_add;
        setMainView(id);

        //Fill spinner
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        String fields[] = new String[]{
                "_id",
                "category_name",
                "category_parent_id"
        };
        Cursor dbCursor = db.select("categories", fields, "category_parent_id", "0");

        //convert cursor to string
        int dbCursorCount = dbCursor.getCount();
        String[] arraySpinnerCategories = new String[dbCursorCount+1];
        arraySpinnerCategories[0] = "This is parent category";

        for(int x =1; x<dbCursorCount+1; x++){
            arraySpinnerCategories[x] = dbCursor.getString(1).toString();
            dbCursor.moveToNext();
        }
        //populate spinner
        Spinner spinnerCategories = (Spinner)getActivity().findViewById(R.id.spinnerCateAdd);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, arraySpinnerCategories);
        spinnerCategories.setAdapter(adapter);

        //button listener
        Button buttonSave = (Button)getActivity().findViewById(R.id.buttonSaveCateAdd);
        buttonSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                buttonSaveOnClick();
            }
        });

        db.close();
    }

    public void buttonSaveOnClick(){
        DBAdapter db = new DBAdapter(getActivity());
        db.open();
        int error = 0;

        EditText edName = (EditText)getActivity().findViewById(R.id.edCateAdd);
        String stringName = edName.getText().toString();
        if(stringName.equals("")){
            Toast.makeText(getActivity(), "Please fill in a category", Toast.LENGTH_LONG).show();
            error = 1;
        }

        Spinner spinner = (Spinner)getActivity().findViewById(R.id.spinnerCateAdd);
        String stringCate = spinner.getSelectedItem().toString();
        String stringParentID;
        int parentID = 0;
        if(stringCate.equals("This is parent category")){
            //stringParentID = "0";
            parentID = 0;
        }
        else {
            String stringCateSQL = db.quoteSmart(stringCate);
            String fields[] = new String[]{
                    "_id",
                    "category_name",
                    "category_parent_id"
            };
            Cursor findParentID = db.select("categories", fields, "category_name", stringCateSQL);
            //stringParentID = findParentID.getString(0);
            parentID = findParentID.getInt(0);

            //parentID = Integer.parseInt(stringParentID);
        }

        if(error == 0){
            //insert into db
            String stringNameSQL = db.quoteSmart(stringName);
            int intIDSQL = db.quoteSmart(parentID);

            String input = "NULL, " + stringNameSQL + "," + intIDSQL;
            db.insert("categories", "_id, category_name, category_parent_id", input);

            Toast.makeText(getActivity(), "Category created", Toast.LENGTH_LONG).show();
        }
        db.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_category, container, false);
        return mainView;
    }

    private void setMainView(int id){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(id, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(mainView);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}