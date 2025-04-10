package AwtSwing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class BingoGame {
	static JPanel panelNorth; // 메뉴 화면
	static JPanel panelWest; // 점수 화면
	static JPanel panelCenter; // 시도 횟수 화면
	static JPanel panelEast; // 메인 화면 버튼 패널
	static JPanel panelSouth; // 게임 화면
	static JLabel labelMessage; // 메뉴 메시지
	static JLabel labelScore; // 점수 메시지
	static JLabel labelTry; // 시도 횟수 메시지
	static JButton buttonMain; // 메인 화면 버튼
	static JButton[] buttons = new JButton[16]; // 카드 버튼
	static JButton[] buttons2 = new JButton[12]; // 카드 버튼 2
	static String[] images = { "image01.png", "image02.png", "image03.png", "image04.png", "image05.png", "image06.png",
			"image07.png", "image08.png", "image01.png", "image02.png", "image03.png", "image04.png", "image05.png",
			"image06.png", "image07.png", "image08.png" }; // 카드 이미지
	static String[] images2 = { "s02.png", "s03.png", "s04.png", "s05.png", "s06.png", "s07.png", "s08.png", "s09.png",
			"s10.png", "s11.png", "s12.png", "s13.png" }; // 카드 이미지 2
	static String[] checkImages = { "s02.png", "s03.png", "s04.png", "s05.png", "s06.png", "s07.png", "s08.png",
			"s09.png", "s10.png", "s11.png", "s12.png", "s13.png" }; // 확인용 이미지
	static boolean[] openCheck = { false, false, false, false, false, false, false, false, false, false, false, false,
			false, false, false, false }; // 오픈 성공 여부
	static int openCount = 0; // 카드 오픈 횟수 (0~2, 0~16)
	static int buttonIndexSave1 = 0; // 첫번째 카드 오픈 인덱스 (0~15)
	static int buttonIndexSave2 = 0; // 두번째 카드 오픈 인덱스 (0~15)
	static int tryCount = 0; // 시도 횟수
	static int successCount = 0; // 성공한 횟수 (0~8)
	static int score = 0; // 현재 점수
	static int chainBonus = 0; // 연속으로 맞췄는지의 여부
	static boolean wait = false; // 대기 변수
	static Timer timer; // 대기 타이머
	static Clip bgm = null; // 배경 음악을 저장하는 변수

	// 메인화면 프레임
	@SuppressWarnings("serial")
	static class MainFrame extends Frame {
		Label lbl;
		Label lbl2;

		MainFrame() {
			// 프레임 및 레이아웃 설정
			super("Bingo Game");
			this.setVisible(true);
			this.setBounds(50, 50, 415, 535);
			this.setLayout(null);
			this.addWindowListener(new MyWinExit());

			playMusic("bgm2.wav"); // 배경 음악 재생

			// start와 challange 라벨 설정
			lbl = new Label("START", 1);
			lbl2 = new Label("CHALLANGE", 1);
			lbl.setBackground(Color.CYAN);
			lbl.setBounds(150, 350, 120, 25);
			add(lbl);
			lbl.addMouseListener(new menuMouseListener(this, 0));
			lbl2.setBackground(Color.RED);
			lbl2.setBounds(150, 400, 120, 25);
			add(lbl2);
			lbl2.addMouseListener(new menuMouseListener(this, 1));

			// 페인 설정
			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.setSize(400, 500);
			layeredPane.setLayout(null);

			// 패널 설정
			myPanel panel = new myPanel();
			panel.setSize(300, 300);
			layeredPane.add(panel);
			setLayout(null);
			add(layeredPane);
			setVisible(true);
			setResizable(false); // 창 크기 수정불가
		}

		class myPanel extends JPanel {
			// paint() 메소드 구현
			public void paint(Graphics g) {
				Font font = new Font("HoonWhitecatR", Font.BOLD, 30);
				g.setFont(font);
				g.drawString("Bingo Game", 120, 175);
			}
		}

		// 윈도우 종료 클래스
		public class MyWinExit extends WindowAdapter {
			public void windowClosing(WindowEvent we) {
				System.exit(0); // 윈도우 종료
			}
		}
	}

	// 게임화면 프레임
	@SuppressWarnings("serial")
	public static class MyFrame extends JFrame implements ActionListener {
		public MyFrame(String title) {
			// 레이아웃 설정
			super(title);
			this.setLayout(new BorderLayout());
			this.setSize(400, 500);
			this.setLocation(50, 50);
			this.setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			initUI(this); // UI 설정
			mixCard(); // 카드 순서를 섞는 메소드
			playMusic("bgm.wav");
			this.pack();
		}

		// 버튼 클릭 오버라이딩
		@Override
		public void actionPerformed(ActionEvent e) {
			// openCount가 2인 동안 인식 X
			if (openCount == 2) {
				return;
			}

			// 클릭한 버튼의 인덱스를 index에 저장한 후, images[index]에 해당하는 이미지로 변경
			JButton btn = (JButton) e.getSource();
			int index = getButtonIndex(btn);
			btn.setIcon(changeImage(images[index]));

			// 이미 오픈된 카드는 인식 X
			if (!openCheck[index]) {
				openCount++;
				if (openCount == 1) {
					buttonIndexSave1 = index; // 첫번째 카드 오픈 인덱스를 저장
				} else if (openCount == 2) {
					buttonIndexSave2 = index; // 두번째 카드 오픈 인덱스를 저장
					if (buttonIndexSave1 != buttonIndexSave2) {
						tryCount++; // 시도 횟수 증가
						labelTry.setText("   시도 횟수: " + tryCount);

						boolean isBingo = checkCard(buttonIndexSave1, buttonIndexSave2); // 빙고인지 확인하는 메소드
						if (isBingo) {
							playSound("bingo.wav"); // 성공 효과음 재생
							openCount = 0; // openCount를 초기화
							successCount++;
							chainBonus++;
							score += (100 * chainBonus);
							labelScore.setText("     Score: " + score);
							openCheck[buttonIndexSave1] = true;
							openCheck[buttonIndexSave2] = true; // 카드 오픈 상태로 유지
							if (successCount == 8) {
								if (tryCount <= 8) {
									score += 2000;
									labelMessage.setText("퍼펙트 클리어!");
								} else if (tryCount <= 15) {
									score += 1000;
									labelMessage.setText("게임 클리어!");
								} else {
									labelMessage.setText("게임 클리어!");
								}
								labelScore.setText("     Score: " + score); // 게임 종료
							}
						} else {
							playSound("fail.wav"); // 실패 효과음 재생
							score -= 10;
							labelScore.setText("     Score: " + score);
							backToQuestion(); // 카드 뒷면 상태로 되돌리는 메소드
						}
					} else {
						openCount--; // 두 인덱스가 동일, 즉 같은 카드를 선택했을 경우 인식 X
					}
				}
			}
		}
	}

	// 게임화면2 프레임
	@SuppressWarnings("serial")
	public static class MyFrame2 extends JFrame implements ActionListener {
		public MyFrame2(String title) {
			// 레이아웃 설정
			super(title);
			this.setLayout(new BorderLayout());
			this.setSize(400, 500);
			this.setLocation(50, 50);
			this.setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			initUI2(this); // UI 설정
			mixCard2(); // 카드 순서를 섞는 함수
			playMusic("bgm3.wav");
			this.pack();
		}

		// 버튼 클릭 오버라이딩
		@Override
		public void actionPerformed(ActionEvent e) {
			if (wait) {
				return; // wait이 true일 경우 입력 무시
			}

			// index 값을 받아 images2[index]에 해당하는 이미지를 체인지
			JButton btn = (JButton) e.getSource();
			int index = getButtonIndex2(btn);
			btn.setIcon(changeImage(images2[index]));

			// 오픈되어있는 카드가 아닐 경우
			if (!openCheck[index]) {
				openCount++;
				if (checkCard(index)) { // images2[index]와 checkImage[openCount-1]를 비교
					playSound("bingo.wav");
					openCheck[index] = true; // 해당 카드를 오픈 상태로 함
					if (openCount == 12) {
						tryCount++;
						labelTry.setText("   시도 횟수: " + tryCount);
						labelMessage.setText("게임 클리어!"); // 게임 종료
					}
				} else {
					wait = true; // 대기 상태 시작
					tryCount++;
					labelTry.setText("   시도 횟수: " + tryCount);
					playSound("fail.wav");
					backToQuestion2(); // 모든 카드를 원래대로 되돌림
				}
			}
		}
	}

	// 마우스 이벤트 인터페이스 구현 (클릭)
	public static class menuMouseListener implements MouseListener {
		MainFrame mainFrame;
		int select;

		menuMouseListener(MainFrame mainFrame, int select) {
			this.mainFrame = mainFrame;
			this.select = select;
		}

		public void mouseClicked(MouseEvent arg0) {
			stopMusic(); // 배경 음악 중단
			mainFrame.setVisible(false);
			// 프레임 전환
			if (select == 0) {
				new MyFrame("Bingo Game Normal");
			} else {
				new MyFrame2("Bingo Game Challenge");
			}
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}

	// 메인화면으로 돌아가는 액션 리스너 구현
	public static class returnMainActionListener implements ActionListener {
		Frame myFrame;

		returnMainActionListener(Frame myFrame) {
			this.myFrame = myFrame;
		}

		public void actionPerformed(ActionEvent e) {
			initGame(); // 게임 데이터 초기화
			stopMusic();
			new MainFrame(); // 메인화면 프레임 생성
			myFrame.setVisible(false);
		}
	}
	
	// 게임1의 UI(패널 및 라벨, 버튼) 설정
	public static void initUI(Frame myFrame) {
		panelNorth = new JPanel();
		panelNorth.setPreferredSize(new Dimension(400, 50));
		panelNorth.setBackground(Color.BLACK);
		labelMessage = new JLabel("같은 그림을 찾아라!");
		labelMessage.setPreferredSize(new Dimension(400, 40));
		labelMessage.setForeground(Color.WHITE);
		labelMessage.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
		labelMessage.setHorizontalAlignment(JLabel.CENTER);
		panelNorth.add(labelMessage);
		myFrame.add("North", panelNorth);

		panelWest = new JPanel();
		panelWest.setPreferredSize(new Dimension(150, 50));
		panelWest.setBackground(Color.WHITE);
		labelScore = new JLabel("     Score: " + score);
		labelScore.setPreferredSize(new Dimension(150, 40));
		labelScore.setForeground(Color.BLACK);
		labelScore.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
		panelWest.add(labelScore);
		myFrame.add("West", panelWest);

		panelCenter = new JPanel();
		panelCenter.setPreferredSize(new Dimension(150, 50));
		panelCenter.setBackground(Color.WHITE);
		labelTry = new JLabel("   시도 횟수: " + tryCount);
		labelTry.setPreferredSize(new Dimension(150, 40));
		labelTry.setForeground(Color.BLACK);
		labelTry.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
		panelCenter.add(labelTry);
		myFrame.add("Center", panelCenter);

		panelEast = new JPanel();
		panelEast.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
		panelEast.setPreferredSize(new Dimension(100, 50));
		panelEast.setBackground(Color.WHITE);
		buttonMain = new JButton("메인");
		buttonMain.setPreferredSize(new Dimension(60, 30));
		buttonMain.addActionListener(new returnMainActionListener(myFrame));
		panelEast.add(buttonMain);
		myFrame.add("East", panelEast);

		panelSouth = new JPanel();
		panelSouth.setLayout(new GridLayout(4, 4));
		panelSouth.setPreferredSize(new Dimension(400, 400));
		for (int i = 0; i < 16; i++) {
			buttons[i] = new JButton();
			buttons[i].setPreferredSize(new Dimension(100, 100));
			buttons[i].setIcon(changeImage("card.png"));
			buttons[i].addActionListener((ActionListener) myFrame);
			panelSouth.add(buttons[i]);
		}
		myFrame.add("South", panelSouth);
	}

	// 게임2의 UI 설정
	public static void initUI2(Frame myFrame) {
		panelNorth = new JPanel();
		panelNorth.setPreferredSize(new Dimension(400, 50));
		panelNorth.setBackground(Color.BLACK);
		labelMessage = new JLabel("순서대로 뒤집어라! (2 → K)");
		labelMessage.setPreferredSize(new Dimension(400, 40));
		labelMessage.setForeground(Color.WHITE);
		labelMessage.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
		labelMessage.setHorizontalAlignment(JLabel.CENTER);
		panelNorth.add(labelMessage);
		myFrame.add("North", panelNorth);

		panelCenter = new JPanel();
		panelCenter.setPreferredSize(new Dimension(300, 50));
		panelCenter.setBackground(Color.WHITE);
		labelTry = new JLabel("시도 횟수: " + tryCount);
		labelTry.setPreferredSize(new Dimension(300, 40));
		labelTry.setForeground(Color.BLACK);
		labelTry.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
		labelTry.setHorizontalAlignment(JLabel.CENTER);
		panelCenter.add(labelTry);
		myFrame.add("Center", panelCenter);

		panelEast = new JPanel();
		panelEast.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
		panelEast.setPreferredSize(new Dimension(100, 50));
		panelEast.setBackground(Color.WHITE);
		buttonMain = new JButton("메인");
		buttonMain.setPreferredSize(new Dimension(60, 30));
		buttonMain.addActionListener(new returnMainActionListener(myFrame));
		panelEast.add(buttonMain);
		myFrame.add("East", panelEast);

		panelSouth = new JPanel();
		panelSouth.setLayout(new GridLayout(4, 3));
		panelSouth.setPreferredSize(new Dimension(400, 399));
		for (int i = 0; i < 12; i++) {
			buttons2[i] = new JButton();
			buttons2[i].setPreferredSize(new Dimension(100, 133));
			buttons2[i].setIcon(changeImage("pokerCard.png"));
			buttons2[i].addActionListener((ActionListener) myFrame);
			panelSouth.add(buttons2[i]);
		}
		myFrame.add("South", panelSouth);
	}

	// 게임 데이터 초기화 메소드
	public static void initGame() {
		for (int i = 0; i < 16; i++) {
			openCheck[i] = false;
		}
		openCount = 0;
		tryCount = 0;
		successCount = 0;
		score = 0;
		chainBonus = 0;
	}

	// 이미지를 바꾸는 메소드
	public static ImageIcon changeImage(String filename) {
		ImageIcon icon = new ImageIcon("./Image/" + filename);
		Image originImage = icon.getImage();
		Image changedImage = originImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH); // 크기 조정
		ImageIcon iconNew = new ImageIcon(changedImage);
		return iconNew;
	}

	// 게임1에서 버튼의 인덱스를 반환하는 메소드
	public static int getButtonIndex(JButton btn) {
		int index = 0;
		for (int i = 0; i < 16; i++) {
			if (buttons[i] == btn) {
				index = i;
			}
		}
		return index;
	}

	// 게임2에서 버튼의 인덱스를 반환하는 메소드
	public static int getButtonIndex2(JButton btn) {
		int index = 0;
		for (int i = 0; i < 12; i++) {
			if (buttons2[i] == btn) {
				index = i;
			}
		}
		return index;
	}
	
	// 게임1에서 카드를 섞는 메소드
	public static void mixCard() {
		Random rand = new Random();
		for (int i = 0; i < 1000; i++) {
			int random = rand.nextInt(15) + 1; // 1~15
			String temp = images[0];
			images[0] = images[random];
			images[random] = temp;
		}
	}

	// 게임2에서 카드를 섞는 메소드
	public static void mixCard2() {
		Random rand = new Random();
		for (int i = 0; i < 1000; i++) {
			int random = rand.nextInt(11) + 1; // 1~11
			String temp = images2[0];
			images2[0] = images2[random];
			images2[random] = temp;
		}
	}

	// 두 카드의 일치 여부를 확인하는 메소드
	public static boolean checkCard(int index1, int index2) {
		if (index1 == index2) {
			return false;
		}
		if (images[index1].equals(images[index2])) {
			return true;
		} else {
			return false;
		}
	}

	// images2[index]와 checkImages[openCount-1]을 비교하는 메소드
	public static boolean checkCard(int index) {
		if (images2[index].equals(checkImages[openCount - 1])) {
			return true;
		} else {
			return false;
		}
	}

	// 오픈되어있는 2개의 카드를 원래대로 되돌리고 초기화하는 메소드
	public static void backToQuestion() {
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openCount = 0;
				chainBonus = 0;
				buttons[buttonIndexSave1].setIcon(changeImage("card.png"));
				buttons[buttonIndexSave2].setIcon(changeImage("card.png"));
				timer.stop();
			}
		});
		timer.start();
	}

	// 모든 카드를 원래 상태로 되돌리고 초기화하는 메소드
	public static void backToQuestion2() {
		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openCount = 0;
				for (int i = 0; i < 12; i++) {
					buttons2[i].setIcon(changeImage("pokerCard.png"));
				}
				for (int i = 0; i < 12; i++) {
					openCheck[i] = false;
				}
				wait = false;
				timer.stop();
			}
		});
		timer.start();
	}

	// 배경 음악을 재생하는 메소드
	public static void playMusic(String filename) {
		File file = new File("./Sound/" + filename); // sound 폴더의 filename 파일
		if (file.exists()) {
			try {
				AudioInputStream stream = AudioSystem.getAudioInputStream(file);
				bgm = AudioSystem.getClip();
				bgm.open(stream);
				bgm.start(); // bgm 재생
				bgm.loop(10); // bgm 반복
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("File Not Found!");
		}
	}

	// 배경 음악을 중단하는 메소드
	public static void stopMusic() {
		if (bgm == null) {
			return;
		}
		bgm.stop();
	}

	// 효과음을 재생하는 메소드
	public static void playSound(String filename) {
		File file = new File("./Sound/" + filename);
		if (file.exists()) {
			try {
				AudioInputStream stream = AudioSystem.getAudioInputStream(file);
				Clip clip = AudioSystem.getClip();
				clip.open(stream);
				clip.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("File Not Found!");
		}
	}

	public static void main(String[] args) {
		new MainFrame(); // 메인화면 프레임 생성
	}
}