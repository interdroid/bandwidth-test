package interdroid.cuckoo.bandwidth;

import interdroid.cuckoo.client.Cuckoo;
import interdroid.cuckoo.client.Cuckoo.Resource;
import interdroid.cuckoo.client.Oracle;
import interdroid.cuckoo.client.Statistics;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.start).setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				v.setEnabled(false);
				new Thread() {
					public void run() {
						testBandwidthMessageSize();
						runOnUiThread(new Runnable() {
							public void run() {
								v.setEnabled(true);
							}
						});

					}
				}.start();
			}
		});
	}

	private String format(double value) {
		return (value == Double.MAX_VALUE ? "\u221E" : ""
				+ String.format("%.2f bytes/ms", value));
	}

	

	private void testBandwidthMessageSize() {
		Oracle.ensureNetwork(this);
		List<Resource> resources = Oracle.getAllResources(this);
//		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
//		System.out.println("link speed before: "
//				+ wifiManager.getConnectionInfo().getLinkSpeed());
		for (Resource resource : resources) {
			int repetitions = 10;
//			 String[] values = new String[] { "512", "1024", "2048", "4096",
//			 "8192", "16384", "32768", "65536", "131072", "262144",
//			 "524288", "1048576" };
			String[] values = new String[] { "16777216" };
//			String[] values = new String[] { "4194304" };
			DescriptiveStatistics[][] measurements = new DescriptiveStatistics[values.length][5];
			Statistics stats = new Statistics();
			String clientUploadMean = "";
			String clientUploadStdev = "";
			String uploadMean = "";
			String uploadStdev = "";
			String downloadMean = "";
			String downloadStdev = "";
			String rttMean = "";
			String rttStdev = "";
			String deltaMean = "";
			String deltaStdev = "";

			for (int j = 0; j < values.length; j++) {
				double dataSize = Integer.parseInt(values[j]);
				// declare
				DescriptiveStatistics[] measurement = new DescriptiveStatistics[5];
				// initialize
				for (int i = 0; i < measurement.length; i++) {
					measurement[i] = new DescriptiveStatistics();
				}
				// run experiment
				for (int i = 0; i < repetitions; i++) {
					Cuckoo.debugServer(resource, stats, (int) dataSize, (int) dataSize, 0);
					measurement[0].addValue(dataSize
							/ Math.max(1, stats.clientUploadTime));
					measurement[1].addValue(dataSize
							/ Math.max(1, stats.uploadTime));
					measurement[2].addValue(dataSize
							/ Math.max(1, stats.downloadTime));
					measurement[3].addValue(stats.rtt);
					measurement[4].addValue(stats.totalInvocationTime
							- stats.uploadTime - stats.executionTime
							- stats.downloadTime - stats.localOverheadTime
							- stats.rtt);
				}
				// store in map
				measurements[j] = measurement;
				// print map
				clientUploadMean += measurement[0].getMean() + ",";
				clientUploadStdev += measurement[0].getStandardDeviation()
						+ ",";
				uploadMean += measurement[1].getMean() + ",";
				uploadStdev += measurement[1].getStandardDeviation() + ",";
				downloadMean += measurement[2].getMean() + ",";
				downloadStdev += measurement[2].getStandardDeviation() + ",";
				rttMean += measurement[3].getMean() + ",";
				rttStdev += measurement[3].getStandardDeviation() + ",";
				deltaMean += measurement[4].getMean() + ",";
				deltaStdev += measurement[4].getStandardDeviation() + ",";

				System.out.println("----- " + values[j] + " -----");
				System.out.println(Arrays.toString(values));
				System.out.println(clientUploadMean);
				System.out.println(clientUploadStdev);
				System.out.println(uploadMean);
				System.out.println(uploadStdev);
				System.out.println(downloadMean);
				System.out.println(downloadStdev);
				System.out.println(rttMean);
				System.out.println(rttStdev);
				System.out.println(deltaMean);
				System.out.println(deltaStdev);

			}
//			System.out.println("link speed after: " + wifiManager.getConnectionInfo().getLinkSpeed());
		}
	}
}
