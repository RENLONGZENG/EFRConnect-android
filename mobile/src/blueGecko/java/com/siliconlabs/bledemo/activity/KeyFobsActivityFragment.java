package com.siliconlabs.bledemo.activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siliconlabs.bledemo.R;
import com.siliconlabs.bledemo.Adapters.KeyFobsRecyclerViewAdapter;
import com.siliconlabs.bledemo.Bluetooth.BLE.Discovery;
import com.siliconlabs.bledemo.Bluetooth.BLE.GattService;
import com.siliconlabs.bledemo.interfaces.FindKeyFobCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class KeyFobsActivityFragment extends Fragment implements Discovery.BluetoothDiscoveryHost {
    private KeyFobsRecyclerViewAdapter adapter;
    private GridLayoutManager layout;
    private RecyclerView.ItemDecoration itemDecoration;
    private Discovery discovery;

    @InjectView(R.id.recyclerview_keyfobs)
    RecyclerView recyclerView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (discovery != null) {
            discovery.connect(context);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_key_fobs, container, false);
        ButterKnife.inject(this, view);

        //force this to 1 column
        layout = new GridLayoutManager(getActivity(), 1, LinearLayoutManager.VERTICAL, false);
        itemDecoration = new RecyclerView.ItemDecoration() {
            final int horizontalMargin = getResources().getDimensionPixelSize(R.dimen.item_margin);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                final int columns = layout.getSpanCount();
                if (columns == 1) {
                    outRect.set(0, 0, 0, 0);
                } else {
                    int itemPos = parent.getChildAdapterPosition(view);
                    if (itemPos % columns == columns - 1) {
                        outRect.set(0, 0, 0, 0);
                    } else {
                        outRect.set(0, 0, horizontalMargin, 0);
                    }
                }
            }
        };
        recyclerView.setLayoutManager(layout);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setHasFixedSize(true);

        adapter = new KeyFobsRecyclerViewAdapter(getActivity(), (FindKeyFobCallback) getActivity());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        discovery = new Discovery(adapter, this);
        discovery.connect(getActivity());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        discovery.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (discovery == null) {
            return;
        }

        adapter.clear();
        adapter.startUpdateHandler();
        reDiscover(true);
    }

    @Override
    public void onPause() {
        adapter.stopUpdateHandler();
        discovery.stopDiscovery(true);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public KeyFobsRecyclerViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public boolean isReady() {
        return isResumed();
    }

    @Override
    public void reDiscover() {
        reDiscover(false);
    }

    @Override
    public void onAdapterDisabled() {

    }

    @Override
    public void onAdapterEnabled() {
        adapter.clear();
        discovery.disconnect();
        discovery.connect(getActivity());
        reDiscover(true);
    }

    private void reDiscover(boolean clearCachedDiscoveries) {
        GattService[] gattServicesForKeyFobs = {/*GattService.LinkLoss,*/ GattService.ImmediateAlert/*, GattService.TxPower*/};
        discovery.clearFilters();
        discovery.addFilter(gattServicesForKeyFobs);
        discovery.startDiscovery(clearCachedDiscoveries);
    }
}
