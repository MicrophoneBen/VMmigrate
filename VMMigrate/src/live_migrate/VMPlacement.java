package live_migrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;

import vmproperty.Host;
import vmproperty.Vm;

public class VMPlacement {
	private static List<Vm> vmlist;
	private static List<Host> hostlist;
	private static HashMap<Vm, ArrayList<Host>> hashmap = new HashMap<Vm, ArrayList<Host>>();

	private static int hostid;
	private static int vmid;

	public static void main(String[] args) {
		hostid = vmid = 0;
		hostlist = new ArrayList<Host>();
		vmlist = new ArrayList<Vm>();
		specHost1(50);
		//specHost2(30);
		specVm1(20);
		specVm2(20);
		PSOSel();
	}

	/**
	 * 8G+4M+4��
	 * 
	 * @param n
	 */
	private static void specHost1(int n) {
		long storage = 102400;
		int hostram = 8192;
		long hostbw = 4000;
		int pesNumOfhost = 4;
		for (int i = 0; i < n; i++) {
			Host host = new Host(hostid, storage, hostram, hostbw, pesNumOfhost);
			hostlist.add(host);
			hostid++;
		}
	}

	/**
	 * 4G+4M+4��
	 * 
	 * @param n
	 */
	private static void specHost2(int n) {
		long storage = 102400;
		int hostram = 4096;
		long hostbw = 4000;
		int pesNumOfhost = 4;
		for (int i = 0; i < n; i++) { // ��ʼ��10����������
			Host host = new Host(hostid, storage, hostram, hostbw, pesNumOfhost);
			hostlist.add(host);
			hostid++;
		}
	}

	/**
	 * 4G+1M+2��
	 * 
	 * @param n
	 */
	private static void specVm1(int n) {
		int userid = 1;
		int ram = 4096; // �ڴ�
		long bw = 1000;// ����
		int pesNumber = 2;
		long size = 10000;
		String vmm = "Xen";
		for (int i = 0; i < n; i++) {
			Vm vm = new Vm(vmid, userid, pesNumber, ram, bw, size, vmm);
			vmlist.add(vm);
			vmid++;
		}
	}

	/**
	 * 2G+1M+1��
	 * 
	 * @param n
	 */
	private static void specVm2(int n) {
		int userid = 1;
		int ram = 2048; // �ڴ�
		long bw = 1000;// ����
		int pesNumber = 1;
		long size = 10000;
		String vmm = "Xen";
		for (int i = 0; i < n; i++) {
			Vm vm = new Vm(vmid, userid, pesNumber, ram, bw, size, vmm);
			vmlist.add(vm);
			vmid++;
		}
	}

	/**
	 * ѡ��һ����������vm
	 * 
	 * @param vm
	 */
	private static void vmToHost(Vm vm) {
		// ƥ����Է��ø�vm�������
		ArrayList<Host> fithostlist = new ArrayList<Host>();
		for (int i = 0; i < hostlist.size(); i++) {
			if (selFitHost(vm, hostlist.get(i))) {
				fithostlist.add(hostlist.get(i));// ������������������������������
			}
		}
		if (fithostlist.size() == 0)
			System.out.println(vm.getId() + "��������޺�����������Է���");
		else {
			hashmap.put(vm, fithostlist); // ���������������������������ӳ��
			// ���vm��host��ӳ���ϵ
			Set<Entry<Vm, ArrayList<Host>>> sets = hashmap.entrySet();
			for (Entry<Vm, ArrayList<Host>> entry : sets) {
				System.out.print(entry.getKey().getId() + "\t");
				for (Host i : entry.getValue()) {
					System.out.print(i.getId() + " ");
				}
				System.out.println();
			}
			randomSel(vm);

			System.out.println(vm.getId() + "\t" + vm.getHost().getId());

		}
	}

	/**
	 * �ж�������ܷ������������������
	 * 
	 * @param vm
	 * @param host
	 * @return
	 */
	public static boolean selFitHost(Vm vm, Host host) {
		int usedCpu = 0;
		double usedMem = 0;
		double usedNet = 0;
		for (int i = 0; i < host.vmlist.size(); i++) {
			usedCpu += host.vmlist.get(i).getNumberOfPes();
			usedMem += host.vmlist.get(i).getRam();
			usedNet += host.vmlist.get(i).getBw();
		}
		if (vm.getBw() > host.getAvailableBw() - usedNet) {
			return false;
		}
		if (vm.getNumberOfPes() > host.getAvailblePes() - usedCpu) {
			return false;
		}
		if (vm.getRam() > host.getAvailableRam() - usedMem) {
			return false;
		}
		return true;
	}

	/**
	 * �Ӻ�ѡ������б������ѡ��һ��
	 * 
	 * @param vm
	 * @return ��ѡ�е�����
	 */
	private static void randomSel(Vm vm) {
		Host value = null;
		// �����������������������ȡһ����������
		int index = (int) (Math.random() * hashmap.get(vm).size());
		value = hashmap.get(vm).get(index);
		value.addVm(vm);
		vm.setHost(value);
		updateHost(value);
	}

	/**
	 * ����������Դ
	 * 
	 * @param host
	 * @param vm
	 */
	public static void updateHost(Host host) {
		for (int i = 0; i < host.vmlist.size(); i++) {
			host.setAvailableBw(host.getAvailableBw()
					- host.vmlist.get(i).getBw());
			host.setAvailableRam(host.getAvailableRam()
					- host.vmlist.get(i).getRam());
			host.setAvailblePes(host.getAvailblePes()
					- host.vmlist.get(i).getNumberOfPes());
		}
	}

	/**
	 * ��׼PSO�㷨
	 */
	private static void PSOSel() {
		//for (int i = 0; i < 15; i++) {
			PSO pso = new PSO(vmlist, hostlist);
			pso.init(100);
			pso.run(100);
			pso.showresult();
			System.out.println();
		//}
	}
}
