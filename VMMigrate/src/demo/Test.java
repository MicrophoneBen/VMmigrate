package demo;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import live_migrate.Particle;
import vmproperty.Host;
import vmproperty.Vm;

/**
 * ����Ⱥ��
 * 
 * @author seven
 * 
 */
public class Test extends RecursiveAction {
	Particle[] pars;
	double global_best;// ȫ��������Ӧ��ֵ
	double global_worst;
	int pcount_all;
	int pcount;// ������Ŀ
	private static int dim;// ά��
	private static int Imax;// ����¼������ֵ

	private static final int THRESHOLD = 20;
	private int start;
	private int end;

	// private static int ms;//������������������Ϊ�����Ƶ��������е�λ��

	public Test(List<Vm> vmList, List<Host> hostList,int start, int end) {
		Particle.vmlist = vmList;
		dim = vmList.size();
		Particle.hostlist = hostList;
		this.start = start;
		this.end = end;
		this.pcount=end-start;
	}

//	public Test(int start, int end) {
//		this.start = start;
//		this.end = end;
//	}

	@Override
	protected void compute() {
		boolean canCompute = start - end <= THRESHOLD;
		if (canCompute) {
			for (int i = start; i < end; i++) {
				pars[i] = new Particle();
				pars[i].init();
				pars[i].evaluate();
				// if (global_best > pars[i].fitness) {
				// global_best = pars[i].fitness;
				// //index = i;
				// }
			}
		} else {
			int mid = (start + end) / 2;
			Test leftTask = new Test(Particle.vmlist,Particle.hostlist,start, mid);
			Test rightTask = new Test(Particle.vmlist,Particle.hostlist,mid + 1, end);

			// ִ��������
			leftTask.fork();
			rightTask.fork();

			// �ȴ�������ִ���꣬���õ����
			leftTask.join();
			rightTask.join();

		}
	}

	/**
	 * ����Ⱥ��ʼ��
	 * 
	 * @param n
	 *            ���ӵ�����
	 */
	public void init(int n) {
		pcount = n;
		global_best = 1;
		int index = -1;// ӵ�����λ�õ����ӱ��
		Imax = 3;
		pars = new Particle[pcount + 1];// ��ʼ����һ�����ӣ�������λ���ٶȵĸ��£�ֻ�����ݴ��м�����
		// ��ľ�̬��Ա�ĳ�ʼ��
		Particle.c1 = 2;
		Particle.c2 = 2;
		Particle.w = 0.9;
		Particle.dims = dim;
		// Particle.m=ms;
		ForkJoinPool forkjoinpool = new ForkJoinPool();
		Test task = new Test(Particle.vmlist,Particle.hostlist,0, pcount);
		forkjoinpool.invoke(task);
		// for (int i = 0; i < pcount; i++) {
		// pars[i] = new Particle();
		// pars[i].init();
		// pars[i].evaluate();
		// if (global_best > pars[i].fitness) {
		// global_best = pars[i].fitness;
		// index = i;
		// }
		//
		// }
		pars[pcount] = new Particle();
		pars[pcount].init();
		System.out.println(global_best);
		Particle.gbest = new int[Particle.dims];
		for (int i = 0; i < dim; i++) {
			Particle.gbest[i] = pars[index].pos[i];
			System.out.print(Particle.gbest[i] + " ");
		}
		System.out.println("\n========init finished!========");
	}

	/**
	 * ����Ⱥ������
	 */
	public void run(int runtimes) {
		System.out.println("=========run start========");
		int cnt = 1;
		int index;
		int idx;
		while (cnt <= runtimes) {
			index = -1;
			idx = -1;
			global_worst = 0;
			// Particle.w=0.9-0.5/runtimes*cnt;
			// ÿ�����Ӹ���λ�ú���Ӧֵ
			for (int i = 0; i < pcount; i++) {
				pars[i].updatev(cnt, runtimes);
				pars[i].evaluate();
				if (global_best > pars[i].fitness) {
					global_best = pars[i].fitness;
					index = i;
				}
				if (global_worst < pars[i].fitness) {
					global_worst = pars[i].fitness;
					idx = i;
				}// Ѱ��ÿ�ε�������Ӧ����������
			}
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < pcount; j++) {
					pars[pcount].pos[i] += pars[j].pos[i];
				}
				pars[pcount].pos[i] = pars[pcount].pos[i] / pcount;
			}// ��������Ⱥλ�õ�ƽ��ֵ�����ڸ��ӵ�������
			if (idx != -1)
				pars[idx].count++;
			for (int i = 0; i < pcount; i++) {
				if (pars[i].count == Imax) {// �����������¼�����ﵽԤ��Ĵ�����������ӽ��н���
					// pars[i].updateParticle(pars[pcount]);
					for (int j = 0; j < dim; j++) {
						pars[i].pos[j] = pars[pcount].pos[i];
					}

				}
				pars[i].count = 0;
			}
			System.out.print(global_best + "    ");
			// ���ָ��õĽ�
			if (index != -1) {
				for (int i = 0; i < dim; i++) {
					Particle.gbest[i] = pars[index].pos[i];
					System.out.print(Particle.gbest[i] + " ");
				}
			}
			System.out.println();
			cnt++;
		}
	}

	/**
	 * ��ʾ���������
	 */
	public void showresult() {
		System.out.println("�㷨��õ����Ž�Ϊ��" + global_best);
		System.out.println("��������õ��������������");
		int j = 0;
		for (int i = 0; i < Particle.dims; i++) {
			System.out.print(Particle.gbest[i] + " ");
			j++;
			if (j == 10) {
				System.out.println();
				j = 0;
			}
		}
	}

}
