package csa.iisc.gaitanalysis.ListAdapters;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import csa.iisc.gaitanalysis.Activities.ExperimentsDashBoardActivity;
import csa.iisc.gaitanalysis.Activities.PerformExperimentActivity;
import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.R;
import csa.iisc.gaitanalysis.Utils.GlobalValues;

public class ExperimentsListAdapter extends RecyclerView.Adapter<ExperimentsListAdapter.ExperimentViewHolder> {


    private static String TAG = "Gait Analysis TrialsAdapter";
    private ExperimentsDashBoardActivity experimentsDashBoardActivity;

//    private List<Experiment> experiments;
    //  public static List<SelectTournamentViewHolder> tournamentViewHolders = new ArrayList<>();

    public ExperimentsListAdapter(ExperimentsDashBoardActivity experimentsDashBoardActivity) {
        this.experimentsDashBoardActivity = experimentsDashBoardActivity;
//        this.experiments = experiments;
    }

    @Override
    public int getItemCount() {
        return GlobalValues.getGlobalValues().getAllExperiments().size();
    }


    @Override
    public void onBindViewHolder(final ExperimentViewHolder experimentViewHolder, final int i) {
        //This can be used to populate the card list
        final Experiment experiment = (GlobalValues.getGlobalValues().getAllExperiments().get(i));

        experimentViewHolder.tvExperimentId.setText(Integer.toString(i + 1));
        experimentViewHolder.tvExperimentName.setText(experiment.getName());
        experimentViewHolder.tvExperimentDateTime.setText(experiment.getDateAndTime());

        if (experiment.isWalkDataPresent())
            experimentViewHolder.tvNoOfWalks.setText(experiment.getWalkData().size() + " Walks");


        experimentViewHolder.btnExperimentDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent performTrials = new Intent(experimentsDashBoardActivity, PerformExperimentActivity.class);
                Bundle extras = new Bundle();
                extras.putInt("experimentIdx", i);
                performTrials.putExtras(extras);
                experimentsDashBoardActivity.startActivity(performTrials);
            }
        });

    }

    @Override
    public ExperimentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_experiment, viewGroup, false);

        ExperimentViewHolder experimentViewHolder = new ExperimentViewHolder(itemView);
        return experimentViewHolder;
    }

    public static class ExperimentViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvExperimentId, tvExperimentName, tvExperimentDateTime, tvNoOfWalks, tvExperimentStatus;
        protected Button btnExperimentDetails;
//        protected ImageView btnCtxtMenu;
//        protected LinearLayout lyCardInfo;
//        protected ToggleButton toggleConnect, toggleStream;

        public ExperimentViewHolder(View v) {
            super(v);
//
            tvExperimentId = (TextView) v.findViewById(R.id.tvExperimentId);
            tvExperimentName = (TextView) v.findViewById(R.id.tvExperimentName);
            tvExperimentDateTime = (TextView) v.findViewById(R.id.tvExperimentDateTime);
            tvNoOfWalks = (TextView) v.findViewById(R.id.tvNoOfWalks);
            tvExperimentStatus = (TextView) v.findViewById(R.id.tvExperimentStatus);


            btnExperimentDetails = (Button) v.findViewById(R.id.btnExperimentDetails);

//            tvSensorName = (TextView) v.findViewById(R.id.sensorName);
//            tvSensorPos = (TextView) v.findViewById(R.id.tvSensorPos);
//            //tvStatus = (TextView) v.findViewById(R.id.tvStatus);
//            tvMacAdd = (TextView) v.findViewById(R.id.tvmacAdd);
//            tvSensorParamInfo = (TextView) v.findViewById(R.id.tvSensorParamInfo);
//
//            btnCtxtMenu = (ImageView) v.findViewById(R.id.btnctxMenu);
//
//            lyCardInfo = (LinearLayout) v.findViewById(R.id.lyCardInfo);
//
//            toggleConnect = (ToggleButton) v.findViewById(R.id.toggleStatus);
//            toggleStream = (ToggleButton) v.findViewById(R.id.toggleStream);

            //  v.setOnCreateContextMenuListener(this);
        }


    }
}