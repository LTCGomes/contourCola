/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculator.application;

import Library.contourCola;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
/**
 *
 * @author Luis
 */
public class CalculatorApplication {
    private String input = "0";
    /*memoria deve guardar o numero em memoria*/
    private float operando1, valorEcra, memoria;
    private char operacao = 'n';
    /*square necessita de ser inicializado*/
    private JTextField ecra,square;
	
    public static final int BORDER = 5;
    public static final int STD_BUTTON_HEIGHT=30;
    public static final int STD_BUTTON_WIDTH=35;
    public static final int LARGE_BUTTON_HEIGHT=30;
    public static final int LARGE_BUTTON_WIDTH=((5*STD_BUTTON_WIDTH+4*BORDER)-2*BORDER)/3;
    public static final Dimension STD_BUTTON_SIZE = new Dimension(STD_BUTTON_WIDTH,STD_BUTTON_HEIGHT);
    public static final Dimension LARGE_BUTTON_SIZE = new Dimension(LARGE_BUTTON_WIDTH,LARGE_BUTTON_HEIGHT); 

    private class BotaoNumeroAccao implements ActionListener {
        int num;
        public BotaoNumeroAccao(int i) {
            num=i;
        }
        public void actionPerformed(ActionEvent e) {
            input = input+Integer.toString(num);
            valorEcra = Float.parseFloat(input);
            imprimeEcra(valorEcra);
        }
    }
    private class OperacaoAccao implements ActionListener {
        char op;
        public OperacaoAccao(char op) {
            this.op = op;
        }
        public void actionPerformed(ActionEvent e) {
            if (operacao=='n') {
                operando1 = valorEcra;
                operacao = op;
            } else {
                valorEcra = executaOp(operacao,operando1,valorEcra);
                operacao = op;
                operando1 = valorEcra;
                imprimeEcra(valorEcra);
            }
            input="0";
        }
    }
    private class IgualAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (operacao!='n') {
                valorEcra = executaOp(operacao,operando1,valorEcra);
            }
            operacao = 'n';
            input = "0";
            imprimeEcra(valorEcra);
        }
    }
    private class PontoAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!input.contains("."))
                input += ".";
        }
    }
    private class BackspaceAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (input.length()!=1) { 
                input = input.substring(0,input.length()-1);
                valorEcra = Float.parseFloat(input);
                imprimeEcra(valorEcra);
            }
        }
    }
    private class CAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            input = "0";
            valorEcra = 0;
            operacao = 'n';
            imprimeEcra(valorEcra);
	}
    }
    private class CEAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            input = "0";
            valorEcra = 0;
            imprimeEcra(valorEcra);
        }
    }
    private class MCAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
        /* ... */
	
        }
    }
    private class MRAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
        /* ... */
	
        }
    }
    private class MSAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
        /* ... */
	
        }
    }
    private class MPAccao implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            
        /* ... */
			
        }
    }
	
    public float executaOp(char operacao, float op1, float op2) {
        switch(operacao) {
            case '+':
                return op1+op2;
            case '-':
                return op1-op2;
            case '*':
                return op1*op2;
            case '/':	
                if (Float.parseFloat(input)!=0)
                    return op1/op2;
                else return 0;
            case 'n':
                return op1;
            default: System.out.println("Operacao nao reconhecida");
        }
        return 0;
    }
	
    public void imprimeEcra(float f) {
        ecra.setText(String.valueOf(f));
    }
	
    public JButton criaBotao(String label, ActionListener a, Dimension tamanho) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(a);
        button.setPreferredSize(tamanho);
        button.setMaximumSize(tamanho);
        button.setMargin(new Insets(3,3,3,3));
        return button;
    }
	
    public JPanel makeCalculator(){
        JTextField lcd;
        JPanel teclado,memoria,clear,panel;
        
        memoria = makeMemoryPad();
        lcd = makeScreen();
        teclado = makeNumberPad();
        clear = makeClearPad();
		
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.add(lcd);
        panel.add(Box.createRigidArea(new Dimension(BORDER,BORDER)));
        panel.add(clear);
        panel.add(Box.createRigidArea(new Dimension(BORDER,BORDER)));
        panel.add(teclado);
        panel.setBorder(new EmptyBorder(BORDER,BORDER,BORDER,BORDER));
                
        return panel;
    }
	
    public JTextField makeScreen() {
        ecra = new JTextField();
        ecra.setEditable(false);
        ecra.setText("");
        ecra.setHorizontalAlignment(JTextField.RIGHT);
        return ecra;
    }
	
    public JPanel makeClearPad() {
        JPanel panel = new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.X_AXIS);
        panel.setLayout(layout);
		
        panel.add(criaBotao("Backspace", new BackspaceAccao(),LARGE_BUTTON_SIZE));
        panel.add(Box.createRigidArea(new Dimension(BORDER,BORDER)));
        panel.add(criaBotao("CE", new CEAccao(),LARGE_BUTTON_SIZE));
        panel.add(Box.createRigidArea(new Dimension(BORDER,BORDER)));
        panel.add(criaBotao("C", new CAccao(),LARGE_BUTTON_SIZE));
        panel.add(Box.createRigidArea(new Dimension(BORDER,BORDER)));
		
        return panel;
    }
	
    public JPanel makeMemoryPad() {
		
        JPanel panel = new JPanel();
        BoxLayout layout =new BoxLayout(panel,BoxLayout.Y_AXIS);
        panel.setLayout(layout);
		
	/* ... */
		
	return panel;
    }
	
    public JPanel makeNumberPad() {
        GridLayout grid = new GridLayout(4,5,BORDER,BORDER);
        JPanel panel = new JPanel(grid);
        JButton button;
        JButton[] numberButtons = new JButton[10];

        for(int i=1; i<=10; i++){
            numberButtons[i%10]=criaBotao(Integer.toString(i%10), new BotaoNumeroAccao(i%10),STD_BUTTON_SIZE);
            panel.add(numberButtons[i%10]);
        }
		
        panel.add(criaBotao("+", new OperacaoAccao('+'),STD_BUTTON_SIZE)); 
        panel.add(criaBotao("-", new OperacaoAccao('-'),STD_BUTTON_SIZE)); 
        panel.add(criaBotao("*", new OperacaoAccao('*'),STD_BUTTON_SIZE)); 
        panel.add(criaBotao("/", new OperacaoAccao('/'),STD_BUTTON_SIZE)); 
		
        panel.add(criaBotao("=", new IgualAccao(),STD_BUTTON_SIZE));
        panel.add(criaBotao(".", new PontoAccao(),STD_BUTTON_SIZE)); 
		
        return panel;
    }
    
    public static boolean checkLicence() throws IOException, Exception {
        
        //System.out.println(obj.getClass().getResource(className));
        //System.out.println(System.getProperty("program.name"));
        contourCola contour = new contourCola("Calculadora", "1.0");
        
        if (contour.isRegister()) {
            return true;
        } else {
            contour.startRegistration();
            return false;
        }
    }
	
    public static void main(String[] args) throws Exception {
        
        if(checkLicence()) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JFrame frame = new JFrame("Calculadora");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CalculatorApplication p = new CalculatorApplication();
            frame.setContentPane(p.makeCalculator());

            frame.setResizable(false);
            frame.pack(); //tem o mesmo efeito que: frame.setSize(frame.getPreferredSize());
            frame.setVisible(true);
        } else {
            System.out.println("No Licence");
        }
    }
}


