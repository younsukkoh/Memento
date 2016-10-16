package younsuk.memento.phasei.pause;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Younsuk on 11/12/2015.
 */
public class MementoListFragment extends Fragment {

    private static final String TAG = "MementoListFragment";

    private RecyclerView mRecyclerView;
    private MementoAdapter mAdapter;
    private FloatingActionButton mAddButton;
    private TextView mEmptyView;

    private ActionMode mActionMode;
    private List<View> mActivatedViews;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.memento_list_fragment, menu);
//        final MenuItem searchItem = menu.findItem(R.id.menu_memento_list_fragment_search);
//        SearchView searchView = (SearchView)searchItem.getActionView();
//        searchItem.setActionView(searchView);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                List<Memento> queriedMementos = new ArrayList<>();
//                List<Memento> mementos = MementoLab.get(getActivity()).getMementos();
//                for (Memento memento: mementos){
//                    if (memento.getTitle() != null && memento.getTitle().contains(query))
//                        queriedMementos.add(memento);
//                }
//
//                mAdapter.setMementos(queriedMementos);
//                mAdapter.notifyDataSetChanged();
//
//                searchItem.collapseActionView();
//                mAddButton.setVisibility(View.VISIBLE);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) { return false; }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_memento_list_fragment_search:
                SearchView searchView = (SearchView)item.getActionView();
                item.setActionView(searchView);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        List<Memento> queriedMementos = new ArrayList<>();
                        List<Memento> mementos = MementoLab.get(getActivity()).getMementos();
                        for (Memento memento : mementos) {
                            if (memento.getTitle() != null && memento.getTitle().contains(query))
                                queriedMementos.add(memento);
                        }

                        mAdapter.setMementos(queriedMementos);
                        mAdapter.notifyDataSetChanged();

                        item.collapseActionView();
//                        mAddButton.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.memento_list_fragment_main, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.memento_list_fragment_main_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAddButton = (FloatingActionButton)view.findViewById(R.id.memento_list_fragment_main_floatingActionButton);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MementoRecorderActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });

        mEmptyView = (TextView)view.findViewById(R.id.memento_list_fragment_main_textView_emptyView);

        updateRecyclerView();

        return view;
    }

    /** The view holder for recycler view. This takes care of each individual view. */
    private class MementoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private Memento mMemento;
        private ImageButton mVideoThumbnail;
//        private TextView mWhatCategory; //Not Used Yet
        private TextView mWhereTextView;
        private TextView mTitleTextView;
        private TextView mDateTextView;

        private ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.memento_list_fragment_actionmode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_memento_list_fragment_actionMode_delete:
                        MementoLab.get(getActivity()).removeMementos(mAdapter.getActivatedMementos()); //Delete Mementos that are activated (selected/hightlighted)
                        mActionMode.finish(); //Exit action mode, which calls onDestroyActionMode
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                mAdapter.clearActivations(); //Clears all the highlights from action mode.
                updateRecyclerView(); //Shows updated list of mementos + checks if the list is empty
            }
        };

        public MementoHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            mVideoThumbnail = (ImageButton)itemView.findViewById(R.id.memento_list_fragment_holderView_thumbnail);
            mTitleTextView = (TextView)itemView.findViewById(R.id.memento_list_fragment_holderView_title);
            mDateTextView = (TextView)itemView.findViewById(R.id.memento_list_fragment_holderView_when);
            mWhereTextView = (TextView)itemView.findViewById(R.id.memento_list_fragment_holderView_where);
        }

        public void bindMemento(Memento memento){
            mMemento = memento;
            mVideoThumbnail.setImageBitmap(mMemento.getThumbnail());

            String title = mMemento.getTitle();
            mTitleTextView.setText(title);

            mDateTextView.setText(new SimpleDateFormat("'At ' h:mm a ' On ' EEE, MMM d, yyyy").format(mMemento.getDate()));

            mWhereTextView.setText(mMemento.getAddress());
        }

        @Override
        public void onClick(View view) {
            if (mActionMode == null){
                Intent intent = MementoFragment_PagerActivity.newIntent(getActivity(), mMemento.getFile());
                startActivity(intent);
            }
            else {
                boolean activated = mAdapter.activateItem(getAdapterPosition());
                view.setActivated(activated);
                if (activated)
                    mActivatedViews.add(view);
                else
                    mActivatedViews.remove(view);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mActionMode == null){
                mActionMode = getActivity().startActionMode(mActionModeCallBack); //starts Action Mode
                view.setActivated(mAdapter.activateItem(getAdapterPosition())); //Activate the item that was long clicked
                mActivatedViews.add(view); //Add the item that was activated to the list of views that are highlighted
            }
            return true;
        }
    }

    /** Adapter for recycler view. This takes care of the entire recycler view as a whole. */
    private class MementoAdapter extends RecyclerView.Adapter<MementoHolder> {

        private List<Memento> mMementos;
        private HashMap mActivatedMementos;

        public MementoAdapter(List<Memento> mementos){
            mMementos = mementos;
            mActivatedMementos = new HashMap();
            mActivatedViews = new ArrayList<>();
        }

        @Override
        public MementoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.memento_list_fragment_holderview, parent, false);

            return new MementoHolder(view);
        }

        @Override
        public void onBindViewHolder(MementoHolder holder, int position) {
            Memento memento = mMementos.get(position);
            holder.bindMemento(memento);
        }

        @Override
        public int getItemCount() { return mMementos.size(); }

        /** Replaces the old list of mementos with the updated list */
        public void setMementos(List<Memento> mementos){ mMementos = mementos; }

        /** Keep track of the list (mActivatedMementos) so that we know which items are activated.
         *  @return True, if the item was not activated, but now it is. False, if the item was already activated, now it is not.
         */
        public boolean activateItem(int position){
            if (mActivatedMementos.get(position) == null){
                mActivatedMementos.put(new Integer(position), mMementos.get(position));
                return true;
            }
            else {
                mActivatedMementos.remove(new Integer(position));
                return false;
            }
        }

        /** Clear all activations  */
        public void clearActivations(){
            mActivatedMementos.clear();

            for (int i = 0; i < mActivatedViews.size(); i ++)
                mActivatedViews.get(i).setActivated(false);

            notifyDataSetChanged();
        }

        public List<Memento> getActivatedMementos(){
            List<Memento> mementos = new ArrayList<>(); //List of mementos to be deleted
            for (int i = 0; i < mMementos.size(); i ++) {
                Memento memento = (Memento) mActivatedMementos.get(new Integer(i));
                if (memento != null)
                    mementos.add(memento);
            }
            return mementos;
        }

        public void getSearchedMementos(int i){

        }
    }

    /** Updates recycler view so that it adjusts to addition or removal of Memento, and also checks if the list is empty*/
    private void updateRecyclerView(){
        MementoLab mementoLab = MementoLab.get(getActivity());
        List<Memento> mementos = mementoLab.getMementos();

        if (mAdapter == null){
            mAdapter = new MementoAdapter(mementos);
            mRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setMementos(mementos);
            mAdapter.notifyDataSetChanged();
        }

        //updateEmptyView(): checks if the list is empty and appropriately displays the hidden text.
        if (mAdapter.getItemCount() == 0)
            mEmptyView.setVisibility(View.VISIBLE);
        else
            mEmptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateRecyclerView();
    }

}
