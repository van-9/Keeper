package com.example.keeper.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keeper.BillAdapter;
import com.example.keeper.BillItem;
import com.example.keeper.R;
import com.example.keeper.activities.EditActivity;
import com.example.keeper.activities.MainActivity;
import com.example.keeper.mytools.MyRecyclerView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.jetbrains.annotations.TestOnly;
import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class BillListFragment extends Fragment {

    private static final DecimalFormat df = new DecimalFormat("¥###,###,##0.00");
    public View view;
    public String TAG;
    public List<BillItem> billItemList;
    public BillAdapter billAdapter;
    public MyRecyclerView billRecyclerView;

    public MainActivity mActivity;
    TextView textTotalIncome;
    TextView textTotalPayout;
    TextView textTotalAmount;
    public boolean isFirstShow = true;
    public static final int REQUEST_ADD_BILL = 0;
    public static final int REQUEST_EDIT_BILL = 1;
    String[] queryConditions;
    String[] queryArguments;

    public static BillListFragment newInstance(String TAG) {
        BillListFragment fragment = new BillListFragment();
        fragment.TAG = TAG;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            queryConditions = bundle.getStringArray("queryConditions");
            queryArguments = bundle.getStringArray("queryArguments");
            TAG = bundle.getString("TAG");
        }
        billItemList = LitePal.where(getMergedQueryString()).order("timeMills desc").find(BillItem.class);
        billAdapter = new BillAdapter(this, billItemList);
        setRetainInstance(true);
        Log.d(TAG, "onCreate: ");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_billlist, container, false);
        textTotalIncome = view.findViewById(R.id.text_income_amount);
        textTotalPayout = view.findViewById(R.id.text_payout_amount);
        textTotalAmount = view.findViewById(R.id.text_total_amount);
        billRecyclerView = view.findViewById(R.id.bill_recyclerView);
        billRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        billRecyclerView.setAdapter(billAdapter);
        billRecyclerView.setHasFixedSize(true);
        final StickyRecyclerHeadersDecoration headersDecoration = new StickyRecyclerHeadersDecoration(billAdapter);
        billRecyclerView.addItemDecoration(headersDecoration);
        billRecyclerView.setEmptyView(view.findViewById(R.id.empty_view));
        return view;
    }


    String getMergedQueryString() {
        StringBuilder sb = new StringBuilder();
        if (queryArguments != null) {
            int length = queryArguments.length;
            for (int i = 0; i < length; ++i) {
                sb.append(queryConditions[i]).append("=").append(queryArguments[i]);
                if (i != length - 1) {
                    sb.append(" and ");
                } else {
                    sb.append(".");
                }
            }
        }
        Log.d(TAG, "getMergedQueryString : " + sb.toString());
        return sb.toString();
    }

    private boolean isMatchCondition(long id) {
        if (queryArguments == null) {
            return true;
        }
        int i = 0, length = queryArguments.length;
        BillItem billItem = LitePal.find(BillItem.class, id);
        outer:
        for (; i < length; ++i) {
            switch (queryConditions[i]) {
                case "year":
                    if (!String.valueOf(billItem.getYear()).equals(queryArguments[i])) {
                        break outer;
                    }
                    break;
                case "month":
                    if (!String.valueOf(billItem.getMonth()).equals(queryArguments[i])) {
                        break outer;
                    }
                    break;
                case "day":
                    if (!String.valueOf(billItem.getDay()).equals(queryArguments[i])) {
                        break outer;
                    }
                    break;
                default:
            }
        }
        return i == length;
    }

    public void addBill() {
        Intent intent = new Intent(getContext(), EditActivity.class);
        intent.putExtra("action", "add");
        startActivityForResult(intent, REQUEST_ADD_BILL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_BILL:
                if (resultCode == RESULT_OK) {
                    refreshRecyclerViewAfterEdit(data);
                }
                break;
            case REQUEST_EDIT_BILL:
                if (resultCode == RESULT_OK) {
                    refreshRecyclerViewAfterEdit(data);
                }
                break;
            default:
                break;
        }
        refreshAmountOfMoney();
    }

    void onBillItemAdded(long id) {
        Log.d(TAG, "onBillItemAdded: ");
        BillItem billItem = LitePal.find(BillItem.class, id);
        if (isMatchCondition(id)) {
            int newPosition = Collections.binarySearch(billItemList, billItem);
            if (newPosition < 0) newPosition = -newPosition - 1;
            billItemList.add(newPosition, billItem);
            billAdapter.notifyItemInserted(newPosition);
            billRecyclerView.scrollToPosition(newPosition);
        }
    }

    void onBillDeleted(int prePosition) {
        billItemList.remove(prePosition);
        billAdapter.notifyItemRemoved(prePosition);
    }

    void onBillEdited(int prePosition, long id) {
        BillItem billItem = LitePal.find(BillItem.class, id);
        if (isMatchCondition(id)) {
            int newPosition = Collections.binarySearch(billItemList, billItem);
            if (newPosition < 0) newPosition = -newPosition - 1;
            if (newPosition == prePosition) {
                billItemList.set(prePosition, billItem);
                billAdapter.notifyItemChanged(prePosition);
            } else {
                billAdapter.notifyItemRemoved(prePosition);
                billAdapter.notifyItemInserted(newPosition);
                billRecyclerView.scrollToPosition(newPosition);
            }
        } else {
            billAdapter.notifyItemRemoved(prePosition);
        }
    }

    public void reloadData() {
        if (!isFirstShow) {
            billItemList.clear();
            Log.d(TAG, " reloadData");
            billItemList.addAll(LitePal.where(getMergedQueryString()).order("timeMills desc").find(BillItem.class));
            billAdapter.notifyDataSetChanged();
            refreshAmountOfMoney();
        }
        isFirstShow = false;
    }

    private void refreshRecyclerViewAfterEdit(Intent intent) {
        int prePosition = intent.getIntExtra("prePosition", -1);
        long id = intent.getLongExtra("id", -1);
        switch (intent.getStringExtra("action")) {
            case "add":
                onBillItemAdded(id);
                Toast.makeText(getContext(), R.string.saved, Toast.LENGTH_SHORT).show();
                break;
            case "delete":
                onBillDeleted(prePosition);
                Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                break;
            case "edit":
                onBillEdited(prePosition, id);
                Toast.makeText(getContext(), R.string.saved, Toast.LENGTH_SHORT).show();
                break;
        }
        mActivity.fab.show();
    }

    public void refreshAmountOfMoney() {
        float payout = 0;
        float total = 0;
        for (BillItem billItem : billItemList) {
            float price = billItem.getPrice();
            if (billItem.isPayout()) {
                payout += price;
            }
            total += price;
        }
        float income = total - payout;
        textTotalAmount.setText(df.format(total));
        textTotalIncome.setText(df.format(income));
        textTotalPayout.setText(df.format(payout));
    }


    @TestOnly
    public void addBillListRandomly() {
        getBillListRandomly(10).forEach((bill -> {
            bill.save();
            onBillItemAdded(bill.getId());
        }));
        refreshAmountOfMoney();
        billRecyclerView.scrollToPosition(0);
    }

    @TestOnly
    public List<BillItem> getBillListRandomly(int amounts) {
        String[] incomeCategory = getResources().getStringArray(R.array.incomeCategory);
        String[] payoutCategory = getResources().getStringArray(R.array.payoutCategory);
        List<BillItem> billItemList = new ArrayList<>();
        for (int i = 0; i < amounts; ++i) {
            Calendar c = Calendar.getInstance();
            Random random = new Random();
            int year = c.get(YEAR) - Math.abs(random.nextInt(2));
            int month = Math.abs(random.nextInt((year == c.get(YEAR) ? c.get(MONTH) + 1 : 12)));
            int day = Math.abs(random.nextInt(28));
            for(int j=0;j<4;++j) {
                int hour = Math.abs(random.nextInt(24));
                int minute = Math.abs(random.nextInt(60));
                BillItem billItem = new BillItem();
                billItem.setPrice(new Random().nextInt() % 200);
                if (billItem.getPrice() < 0) billItem.setType(BillItem.PAYOUT);
                else billItem.setType(BillItem.INCOME);
                if (billItem.isIncome()) {
                    billItem.setCategory(incomeCategory[Math.abs(new Random().nextInt() % incomeCategory.length)]);
                } else {
                    billItem.setCategory(payoutCategory[Math.abs(new Random().nextInt() % payoutCategory.length)]);
                }

                c.set(year, month, day, hour, minute);
                billItem.setTime(c.getTimeInMillis());
                billItemList.add(billItem);
            }
        }
        return billItemList;
    }
}