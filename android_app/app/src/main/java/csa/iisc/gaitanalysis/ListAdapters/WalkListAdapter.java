package csa.iisc.gaitanalysis.ListAdapters;


import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import csa.iisc.gaitanalysis.Activities.PerformExperimentActivity;
import csa.iisc.gaitanalysis.Activities.ShowGraphActivity;
import csa.iisc.gaitanalysis.DataModel.Experiment;
import csa.iisc.gaitanalysis.DataModel.GlobalConstants;
import csa.iisc.gaitanalysis.DataModel.Walk;
import csa.iisc.gaitanalysis.R;

public class WalkListAdapter extends RecyclerView.Adapter<WalkListAdapter.WalkViewHolder> {


    private static PerformExperimentActivity performExperimentActivity;
    private static String TAG = "Gait Analysis TrialsAdapter";

    private Integer experimentIdx;
    private Experiment experiment;

//    private List<String> walksList;
    //  public static List<SelectTournamentViewHolder> tournamentViewHolders = new ArrayList<>();

    public WalkListAdapter(PerformExperimentActivity performExperimentActivity, Experiment experiment, Integer experimentIdx) {
        WalkListAdapter.performExperimentActivity = performExperimentActivity;
        this.experiment = experiment;
        this.experimentIdx = experimentIdx;

    }

    public void addItem(String walk) {
//        walksList.add(walk);

    }

    @Override
    public int getItemCount() {
        return experiment.getWalkData().size();
    }

    private void viewOnAnglePresent(WalkViewHolder walkViewHolder, final Integer walkIdx) {
//        walkViewHolder.btnCalcAngles.setText("Angles Present");
//        walkViewHolder.btnCalcAngles.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent graph = new Intent(performExperimentActivity, ShowGraphActivity.class);
//                graph.putExtra("experimentIdx", experimentIdx);
//                graph.putExtra("walkIdx", walkIdx);
//                performExperimentActivity.startActivity(graph);
//            }
//        });

        Intent graph = new Intent(performExperimentActivity, ShowGraphActivity.class);
        graph.putExtra("experimentIdx", experimentIdx);
        graph.putExtra("walkIdx", walkIdx);
        performExperimentActivity.startActivity(graph);
    }

    @Override
    public void onBindViewHolder(final WalkViewHolder walkViewHolder, final int i) {
        //This can be used to populate the card list
        //final String walkName = experiment.getWalkData().get(0).getWalkFiles().get(i);

        final Walk currWalk = experiment.getWalkData().get(i);

        walkViewHolder.tvWalkId.setText(Integer.toString(i + 1));
        walkViewHolder.tvWalkName.setText("Walk " + Integer.toString(i + 1));

        String walkFiles = "";
        for (int si = 0; si < currWalk.getWalkFiles().size(); si++) {
            walkFiles = walkFiles + experiment.getWalkData().get(i).getWalkFiles().get(si) + "\n";
        }

        walkViewHolder.tvWalkFiles.setText(walkFiles);

        walkViewHolder.btnCalcAngles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!experiment.isCalibrationDone(GlobalConstants.JointType.RightKnee) && !experiment.isCalibrationDone(GlobalConstants.JointType.RightAnkle)) {
                    Snackbar.make(performExperimentActivity.findViewById(R.id.lyPerformExperimentMainDashboard), "Calibration Not Done", Snackbar.LENGTH_LONG).show();
                    return;
                }

                viewOnAnglePresent(walkViewHolder, i);

            }
        });

        walkViewHolder.imgResetWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                experiment.removeWalk(currWalk);
            }
        });


    }

    @Override
    public WalkViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_walk, viewGroup, false);

        WalkViewHolder walkViewHolder = new WalkViewHolder(itemView);
        return walkViewHolder;
    }

    public static class WalkViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvWalkId, tvWalkName, tvWalkFiles;
        protected Button btnCalcAngles;
        protected ImageView imgResetWalk;

        public WalkViewHolder(View v) {
            super(v);

            tvWalkId = (TextView) v.findViewById(R.id.tvWalkId);
            tvWalkName = (TextView) v.findViewById(R.id.tvWalkName);
            tvWalkFiles = (TextView) v.findViewById(R.id.tvWalkFiles);

            btnCalcAngles = (Button) v.findViewById(R.id.btnCalcAngles);

            imgResetWalk = (ImageView) v.findViewById(R.id.imgResetWalk);


            //  v.setOnCreateContextMenuListener(this);
        }


    }
}