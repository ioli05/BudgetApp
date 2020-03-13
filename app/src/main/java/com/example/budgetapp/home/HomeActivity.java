package com.example.budgetapp.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ListView;

import com.example.budgetapp.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.TranzactionModel;


public class HomeActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    PieChart mPieChart;
    ListView mListView;

    FirebaseUser mCurrentUser;
    FirebaseFirestore db;

    List<DocumentSnapshot> myListOfDocuments;

    int[] colors;

    TranzactionAdapter mTranzactionAdapter;

    ArrayList<TranzactionModel> mTranzactionModelList = new ArrayList<>();
    ArrayList<TranzactionModel> mTranzactionModelListFiltered = new ArrayList<>();

    String selectedPieValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFields();

        db.collection("users").document(mCurrentUser.getUid())
                .collection("tranzactions")
                .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    myListOfDocuments = task.getResult().getDocuments();
                    fetchDatabaseDataListView();
                    initializeAllElementsListView();
                    addToPieCart();
                    addToListViewAdapterElements();
                }
            }
        });
    }

    private void fetchDatabaseDataListView(){
        for(DocumentSnapshot doc : myListOfDocuments) {
            mTranzactionModelList.add(new TranzactionModel((String)doc.getData().get("name"), doc.getDouble("sum"), (String)doc.getData().get("category")));
        }
    }

    private void initializeAllElementsListView() {
        mTranzactionModelListFiltered = (ArrayList<TranzactionModel>) mTranzactionModelList.clone();
    }

    private void refreshData() {
        mTranzactionModelListFiltered.clear();
        mTranzactionModelListFiltered.addAll(mTranzactionModelList);
    }

    private void addToPieCart() {
        PieDataSet mPieDataSet = new PieDataSet(getPieData(), "");

        mPieDataSet.setColors(colors);

        PieData mPieData = new PieData(mPieDataSet);

        mPieChart.setData(mPieData);
        mPieChart.invalidate();
        mPieChart.setHighlightPerTapEnabled(true);

        mPieChart.setOnChartValueSelectedListener(this);
    }

    private void addToListViewAdapterElements() {

        mTranzactionAdapter = new TranzactionAdapter(this, mTranzactionModelListFiltered);
        mListView = findViewById(R.id.tranzactionList);
        mListView.setAdapter(mTranzactionAdapter);

        mListView.setOnItemClickListener((parent, view, position, id) -> {
            //Toast.makeText(getApplicationContext(),"Place Your First Option Code",Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeFields(){
       colors = new int[]{Color.BLACK, Color.CYAN, Color.GREEN, Color.MAGENTA};

       mPieChart = findViewById(R.id.tranzactionPieChart);
       mListView = findViewById(R.id.tranzactionList);

       mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
       db = FirebaseFirestore.getInstance();
    }

    private ArrayList<PieEntry> getPieData(){

        List<String> categories;

        categories =  mTranzactionModelList.stream().map(TranzactionModel::getCategory).distinct().collect(Collectors.toList());
        ArrayList<PieEntry> mPieData = new ArrayList<>();

        for (String current : categories){
            double categorysum = mTranzactionModelList.stream().filter(tranzaction ->tranzaction.getCategory().equals(current)).map(TranzactionModel::getSum).reduce(0.0, Double::sum);
            mPieData.add(new PieEntry((float)categorysum, current));
        }
        return mPieData;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        PieEntry pe = (PieEntry) e;
        selectedPieValue = pe.getLabel();

        mTranzactionAdapter.clear();


        for (TranzactionModel tranzaction : mTranzactionModelList){
            if (tranzaction.getCategory().equals(selectedPieValue)){
                mTranzactionModelListFiltered.add(tranzaction);
            }
        }

        mTranzactionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected() {

        mTranzactionAdapter.clear();
        refreshData();

        mTranzactionAdapter.notifyDataSetChanged();

    }
}
