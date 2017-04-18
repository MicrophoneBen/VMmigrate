package live_migrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vmproperty.Host;
import vmproperty.Vm;

/**
 * ������
 * 
 * @author seven
 * 
 */
public class Particle {

	public int[] pos;// ���ӵ�λ�ã������ά�ȱ�ʾ������ĸ���
	public int[] v;
	public double fitness;
	public int[] pbest; // ���ӵ���ʷ��õ�λ��
	public static int[] gbest; // ���������ҵ������λ��
	public static int dims;
	public static double w;
	public static double c1;
	public static double c2;
	public static Random rnd;
	double pbest_fitness;// ���ӵ���ʷ������Ӧֵ
	int size;// ������������Է��õ���������
	private List<Host> fitList;//
	public static List<Host> hostlist;
	public static List<Vm> vmlist;
	public  int count;//

	public void init() {
		pos = new int[dims];
		v = new int[dims];
		pbest = new int[dims];
		fitness = 1;
		pbest_fitness = 1;
		count=0;
		fitList = hostlist;
		rnd = new Random();
		for (int i = 0; i < dims; i++) {
			int size = fitList.size();
			pos[i] = rnd.nextInt(size);
			// ����ÿ�����ӣ��ڼ���λ�ú��ٶȹ����У�ֻ��vm����host�������б��У���������������Դ
			// �ڶ����ӽ�����Ӧ��ֵ����ʱ�ڸ�����Դ
			fitList.get(pos[i]).addVm(vmlist.get(i));
			pbest[i] = pos[i];
			v[i] = rand(-pos[i], size - pos[i] - 1);
			updateVmList(vmlist.get(i));
		}
	}

	/**
	 * �����ȡ�б��еĶ���
	 * 
	 * @param list
	 * @return
	 */
	public Host getrnd(List<Host> list) {
		int index = (int) (Math.random() * list.size());
		return list.get(index);
	}

	/**
	 * ����low��uper֮�����
	 * 
	 * @param low
	 *            ����
	 * @param uper
	 *            ����
	 * @return ����low��uper֮�����
	 */
	int rand(int low, int uper) {
		rnd = new Random();
		return rnd.nextInt() * (uper - low + 1) + low;
	}

	/**
	 * �жϸ��ؾ���ȣ�����¼��ʷ���Ž� ���ﶨ�帺�ؾ����Ϊ������������Դ�����ʵı�׼��
	 */
	public void evaluate() {
		double[] x = new double[hostlist.size()];
		// �ڶ���������о���ȼ���ʱ�Ÿ���ÿ�����������Դ״̬
		for (int i = 0; i < hostlist.size(); i++) {
			VMPlacement.updateHost(hostlist.get(i));// ����������vmlist��Ÿ���������Դ
			x[i] = hostlist.get(i).getLoad();
		}
		fitness = StandardDiviation(x);
		if (fitness < pbest_fitness) {
			for (int i = 0; i < dims; i++) {
				pbest[i] = pos[i];
			}
			pbest_fitness = fitness;
		}
		resetHost();// ÿ��������������֮��ԭ������Դ����ȷ����һ����������ȷ���㸺��
	}

	/**
	 * ��ԭ�������������ʼ״̬
	 */
	private void resetHost() {
		for (int i = 0; i < hostlist.size(); i++) {
			hostlist.get(i).vmlist = new ArrayList<Vm>();
			hostlist.get(i).setAvailableBw(hostlist.get(i).getBw());
			hostlist.get(i).setAvailableRam(hostlist.get(i).getRam());
			hostlist.get(i).setAvailblePes(hostlist.get(i).getPesNum());
		}
	}

	/**
	 * ���׼��
	 * 
	 * @param x
	 * @return
	 */
	private static double StandardDiviation(double[] x) {
		int m = hostlist.size();
		double sum = 0;
		for (int i = 0; i < m; i++) {// ���
			sum += x[i];
		}
		double dAve = sum / m;// ��ƽ��ֵ
		double dVar = 0;
		for (int i = 0; i < m; i++) {// �󷽲�
			dVar += (x[i] - dAve) * (x[i] - dAve);
		}
		return Math.sqrt(dVar / m);
	}

	/**
	 * �����ٶȺ�λ��
	 */
	public void updatev(int cnt, int runtimes) {
		//double k;
		int �� = 2;
		//���Լ���w����̬������̬����c1��c2
		w = 0.9 - 0.5 / 100 * cnt;
		c1 = 0.5
				+ (4.5 - 0.5)
				/ (Math.sqrt(2 * Math.PI) * ��)
				* Math.exp(-(cnt / runtimes) * (cnt / runtimes)
						/ (2 * �� * ��));
		c2 = 2.5
				+ (0.5 - 2.5)
				/ (Math.sqrt(2 * Math.PI) * ��)
				* Math.exp(-(cnt / runtimes) * (cnt / runtimes)
						/ (2 * �� * ��));
		//k = (0.1 - 1) * (runtimes - cnt) / runtimes + 1;
		for (int i = 0; i < dims; i++) {
			updateVmList(vmlist.get(i));
			size = fitList.size();
			v[i] = (int) (w * v[i] + c1 * rnd.nextDouble()
					* (pbest[i] - pos[i]) + c2 * rnd.nextDouble()
					* (gbest[i] - pos[i]));
			// �����ٶȺ�λ��
			if (v[i] > size - pos[i] - 1) {
				v[i] = size - pos[i] - 1;
			}
			if (v[i] < -pos[i]) {
				v[i] = -pos[i];
			}
			pos[i] = pos[i] + v[i];
			fitList.get(pos[i]).vmlist.add(vmlist.get(i));// ��i��vm�����pos[i]��host
		}
		
	}

	/**
	 * ����ÿ�����������ƥ��������б�
	 */
	private void updateVmList(Vm vm) {
		fitList = new ArrayList<Host>();
		for (int i = 0; i < hostlist.size(); i++) {
			if (VMPlacement.selFitHost(vm, hostlist.get(i))) {
				fitList.add(hostlist.get(i));// ������������������������������
			}
		}
	}
	
	/**
	 * �����ӽ��н�����λ�ó�ʼ��Ϊȫ�����ӵ�����
	 */
	public void updateParticle(Particle a) {
		fitness = 1;
		pbest_fitness = 1;
		count = 0;
		fitList = hostlist;
		for (int i = 0; i < dims; i++) {
			int size = fitList.size();
			pos[i] = a.pos[i];
			// ����ÿ�����ӣ��ڼ���λ�ú��ٶȹ����У�ֻ��vm����host�������б��У���������������Դ
			// �ڶ����ӽ�����Ӧ��ֵ����ʱ�ڸ�����Դ
			pbest[i] = a.pos[i];
			v[i] = rand(-pos[i], size - pos[i] - 1);
		}

	}
}
