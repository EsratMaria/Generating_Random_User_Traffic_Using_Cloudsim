package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;

import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;



/**
 *This is a simple example of assigning cloudlets to
 * Vms in a random manner.
 * This method can also be used to generate user defined 
 * traffic, since in CLoudsim cloudlets(tasks) can be considered
 * as traffic in the system.
 */
public class GeneratingTrafficAndBindingCloudletsWithRandomVms{

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vm list. */
	private static List<Vm> vmList;

	private static int hostsNumber = 5;
	private static double vmsNumber = 10;
	private static double cloudletsNumber = 10;
	public static List<Vm> TotalvmList = new ArrayList<Vm>();
	public static Random r = new Random();

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {

		Log.printLine("Starting simple example...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities. We can't run this example without
			// initializing CloudSim first. We will get run-time exception
			// error.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace GridSim events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter = createDatacenter("Datacenter_0");
			Log.printLine("The datacenter ID is: " + datacenter.getId());

			// Third step: Create Broker

			DatacenterBroker broker = createBroker();

			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmList = createVms(brokerId);

			for (int i = 0; i < vmList.size(); i++) {
				// TotalvmList.add(vmList.get(i));
				Log.printLine(vmList.get(i).getId());
			}
			for (int i = 0; i < vmList.size(); i++)
				TotalvmList.add(vmList.get(i));

			// submit vm list to the broker
			broker.submitVmList(vmList);

			// Fifth step: Create one cloudlet
			cloudletList = createCloudletList(brokerId);

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// bind the cloudlets to the vms. This way, the broker
			// will submit the bound cloudlets only to the specific VM
			for (int i = 0; i < 10; i++) {
				Vm vms = vmList.get(i);
				Cloudlet cloudlet1 = cloudletList.get(i);
				broker.bindCloudletToVm(cloudlet1.getCloudletId(), r.nextInt(vmList.size()));
			}

			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine();

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("Simple example finished!");

	}

	/**
	 * Creates the cloudlet list.
	 *
	 * @param brokerId the broker id
	 *
	 * @return the cloudlet list
	 */
	private static List<Cloudlet> createCloudletList(int brokerId) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long length = 150000;
		int pesNumber = 1;
		long fileSize = 300;
		long outputSize = 300;

		for (int i = 0; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize,
					new UtilizationModelStochastic(), new UtilizationModelStochastic(),
					new UtilizationModelStochastic());
			cloudlet.setUserId(brokerId);
			// cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	/**
	 * Creates the vms.
	 *
	 * @param brokerId the broker id
	 *
	 * @return the list< vm>
	 */
	private static List<Vm> createVms(int brokerId) {
		List<Vm> vms = new ArrayList<Vm>();

		// VM description
		int[] mips = { 250, 500, 750, 1000 }; // MIPSRating
		int pesNumber = 1; // number of cpus
		int ram = 128; // vm memory (MB)
		long bw = 2500; // bandwidth
		long size = 2500; // image size (MB)
		String vmm = "Xen"; // VMM name

		for (int i = 0; i < vmsNumber; i++) {
			vms.add((Vm) new Vm(i, brokerId, mips[i % mips.length], pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerDynamicWorkload(mips[i % mips.length], pesNumber)));
		}

		return vms;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 *
	 * @throws Exception the exception
	 */
	private static Datacenter createDatacenter(String name) throws Exception {
		// Here are the steps needed to create a TrafficDatacenter:
		// 1. We need to create an object of HostList2 to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		int[] mips = { 1000, 2000, 3000 };
		int ram = 10000; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 100000;

		for (int i = 0; i < hostsNumber; i++) {
			// 2. A Machine contains one or more PEs or CPUs/Cores.
			// In this example, it will have only one core.
			// 3. Create PEs and add these into an object of TrafficPeList.
			List<Pe> peList = new ArrayList<Pe>();
			peList.add(new Pe(0, new PeProvisionerSimple(
					mips[i % mips.length])));
			
			hostList.add(new Host(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage,
					peList, new VmSchedulerTimeShared(peList))); // This is our machine
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a Grid resource: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/TrafficPe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		
		Datacenter datacenter = null;

		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 5.0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */

	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "\t";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Resource ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId());

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + "SUCCESS" + indent + indent + cloudlet.getResourceId() + indent
						+ cloudlet.getVmId() + indent + dft.format(cloudlet.getActualCPUTime()) + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

}
