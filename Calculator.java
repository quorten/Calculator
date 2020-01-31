// Calculator.java -- A simple Java calculator.
// Public Domain 2013, 2020 Andrew Makousky

// See the file "UNLICENSE" in the top level directory for details.

// This is free and unencumbered software released into the public
// domain.

// Anyone is free to copy, modify, publish, use, compile, sell, or
// distribute this software, either in source code form or as a
// compiled binary, for any purpose, commercial or non-commercial, and
// by any means.

// In jurisdictions that recognize copyright laws, the author or
// authors of this software dedicate any and all copyright interest in
// the software to the public domain. We make this dedication for the
// benefit of the public at large and to the detriment of our heirs
// and successors. We intend this dedication to be an overt act of
// relinquishment in perpetuity of all present and future rights to
// this software under copyright law.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
// CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
// CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

// For more information, please refer to <http://unlicense.org>

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Calculator implements ActionListener
{
    private JFrame frame;
    private JTextArea textView;

    private double lastNumber, curNumber;
    private char lastOp;
    private boolean afterDec;
    private int decPlace;
    private boolean eqPressed;

    public Calculator()
    {
	// Setup the look and feel.
	String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
	try {
	    UIManager.setLookAndFeel(lookAndFeel);
	}
	catch (Exception e) {
	}

	// Setup the main window.
	frame = new JFrame("Calculator");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(2, 2, 2, 2);
	c.fill = GridBagConstraints.BOTH;

	JPanel panel = new JPanel(gridbag);
	panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	frame.add(panel, BorderLayout.CENTER);

	// Add the numeric buttons.
	JButton[] buttons = new JButton[18];
	int i;
	for (i = 0; i < 10; i++) {
	    buttons[i] = new JButton("" + i);
	    // Warning: shortcut to set key mnemonic
	    buttons[i].setMnemonic(KeyEvent.VK_0 + i);
	    buttons[i].addActionListener(this);
	    if (i != 0) {
		c.gridx = (i - 1) % 3;
		c.gridy = 3 - (i - 1) / 3;
		gridbag.setConstraints(buttons[i], c);
		panel.add(buttons[i]);
	    }
	    else {
		c.gridx = 0; c.gridy = 4;
		gridbag.setConstraints(buttons[i], c);
		panel.add(buttons[i]);
	    }
	}

	// Add the function keys.
	String[] funcLabels = { ".", "+", "-", "*", "/", "=", "C", "CE" };
	int[] funcGxPos     = {  1,   3,   4,   3,   4,   3,   3,   4 };
	int[] funcGyPos     = {  4,   3,   3,   2,   2,   4,   1,   1 };
	int[] funcMnems = { KeyEvent.VK_PERIOD, KeyEvent.VK_PLUS,
			    KeyEvent.VK_MINUS, KeyEvent.VK_ASTERISK,
			    KeyEvent.VK_SLASH, KeyEvent.VK_EQUALS,
			    KeyEvent.VK_C, KeyEvent.VK_E };
	c.gridwidth = 1; c.gridheight = 1;
	while (i < 18) {
	    c.gridx = funcGxPos[i-10];
	    c.gridy = funcGyPos[i-10];
	    if (funcLabels[i-10].equals("="))
		c.gridwidth = 2;
	    else
		c.gridwidth = 1;
	    buttons[i] = new JButton(funcLabels[i-10]);
	    buttons[i].setMnemonic(funcMnems[i-10]);
	    buttons[i].addActionListener(this);
	    gridbag.setConstraints(buttons[i], c);
	    panel.add(buttons[i]);
	    i++;
	}

	// Add the calculator screen.
	c.gridx = 0; c.gridy = 0;
	c.gridwidth = 5; c.gridheight = 1;
	textView = new JTextArea();
	textView.setEditable(false);
	gridbag.setConstraints(textView, c);
	panel.add(textView);

	// Display the window.
	frame.pack();
	frame.setVisible(true);

	// Initialize the calculator state.
	initState();
	textView.setText("" + curNumber);
    }

    public void actionPerformed(ActionEvent e)
    {
	String btnName = ((JButton)e.getSource()).getText();
	char btnChar = btnName.charAt(0);
	switch (btnChar) {

	case '0': case '1': case '2': case '3': case '4': case '5':
	case '6': case '7': case '8': case '9':
	    if (eqPressed)
		initState();
	    if (!afterDec) {
		curNumber = curNumber * 10.0 +
		    Double.parseDouble(btnName);
	    }
	    else {
		double digit = Double.parseDouble(btnName);
		for (int i = 0; i < decPlace; i++)
		    digit /= 10.0;
		curNumber = curNumber + digit;
		decPlace++;
	    }
	    textView.setText("" + curNumber);
	    break;

	case '.':
	    if (eqPressed)
		initState();
	    afterDec = true;
	    textView.setText("" + curNumber);
	    break;

	case '+':
	case '-':
	case '*':
	case '/':
	    if (eqPressed)
		eqPressed = false;
	    else
		compute();
	    lastOp = btnChar;
	    curNumber = 0.0;
	    textView.setText("" + lastNumber);
	    break;

	case '=':
	    eqPressed = true;
	    compute();
	    textView.setText("" + lastNumber);
	    break;

	case 'C':
	    if (btnName.length() == 1)
		initState();
	    else {
		eqPressed = false;
		curNumber = 0.0;
	    }
	    textView.setText("" + curNumber);
	    break;
	}
    }

    private void initState()
    {
	lastNumber = curNumber = 0.0;
	lastOp = '\0';
	afterDec = false;
	decPlace = 1;
	eqPressed = false;
    }

    private void compute()
    {
	switch (lastOp) {
	case '\0':
	    lastNumber = curNumber;
	    break;
	case '+':
	    lastNumber = lastNumber + curNumber;
	    break;
	case '-':
	    lastNumber = lastNumber - curNumber;
	    break;
	case '*':
	    lastNumber = lastNumber * curNumber;
	    break;
	case '/':
	    lastNumber = lastNumber / curNumber;
	    break;
	}
	afterDec = false;
	decPlace = 1;
    }

    public static void main(String[] args)
    {
	Calculator calc = new Calculator();
    }
}
