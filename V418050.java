import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Vector;
import java.sql.*;

class Patient {
    int patientID;
    String name;
    int age;
    String disease;

    public Patient(int patientID, String name, int age, String disease) {
        this.patientID = patientID;
        this.name = name;
        this.age = age;
        this.disease = disease;
    }

    public void displayPatientDetails() {
        System.out.println("Patient ID: " + patientID);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Disease: " + disease);
    }
}

class Appointment {
    int appointmentID;
    int patientID;
    Date appointmentDate;
    String doctorAssigned;

    public Appointment(int appointmentID, int patientID, Date appointmentDate, String doctorAssigned) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.appointmentDate = appointmentDate;
        this.doctorAssigned = doctorAssigned;
    }

    public void displayAppointmentDetails() {
        System.out.println("Appointment ID: " + appointmentID);
        System.out.println("Patient ID: " + patientID);
        System.out.println("Appointment Date: " + appointmentDate);
        System.out.println("Doctor Assigned: " + doctorAssigned);
    }
}

class Ward {
    int wardNumber;
    int capacity;
    Vector<Patient> assignedPatients;

    public Ward(int wardNumber, int capacity) {
        this.wardNumber = wardNumber;
        this.capacity = capacity;
        this.assignedPatients = new Vector<>();
    }

    public boolean assignPatient(Patient patient) {
        if (assignedPatients.size() < capacity) {
            assignedPatients.add(patient);
            return true;
        } else {
            return false;
        }
    }

    public void displayWardDetails() {
        System.out.println("Ward Number: " + wardNumber);
        System.out.println("Capacity: " + capacity);
        System.out.println("Assigned Patients: ");
        for (Patient patient : assignedPatients) {
            patient.displayPatientDetails();
        }
    }
}

class Bill {
    int patientID;
    double totalCost;
    String servicesProvided;

    public Bill(int patientID, double totalCost, String servicesProvided) {
        this.patientID = patientID;
        this.totalCost = totalCost;
        this.servicesProvided = servicesProvided;
    }

    public void displayBillDetails() {
        System.out.println("Patient ID: " + patientID);
        System.out.println("Total Cost: " + totalCost);
        System.out.println("Services Provided: " + servicesProvided);
    }
}

public class V418050 {
    static Vector<Patient> patients = new Vector<>();
    static Vector<Appointment> appointments = new Vector<>();
    static Vector<Ward> wards = new Vector<>();
    static Vector<Bill> bills = new Vector<>();

    // JDBC variables
    private static final String URL = "jdbc:mysql://localhost:3307/hospital";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection connection;

    JFrame frame;
    JTextArea textArea;
    JTextField txtPatientID, txtName, txtAge, txtDisease, txtAppointmentID, txtDoctorAssigned, txtWardNumber, txtCapacity, txtTotalCost, txtServicesProvided;

    public V418050() {
        frame = new JFrame("Hospital Management System");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        frame.add(panel, BorderLayout.NORTH);

        // Patient Inputs
        panel.add(new JLabel("Patient ID:"));
        txtPatientID = new JTextField();
        panel.add(txtPatientID);

        panel.add(new JLabel("Patient Name:"));
        txtName = new JTextField();
        panel.add(txtName);

        panel.add(new JLabel("Age:"));
        txtAge = new JTextField();
        panel.add(txtAge);

        panel.add(new JLabel("Disease:"));
        txtDisease = new JTextField();
        panel.add(txtDisease);

        // Appointment Inputs
        panel.add(new JLabel("Appointment ID:"));
        txtAppointmentID = new JTextField();
        panel.add(txtAppointmentID);

        panel.add(new JLabel("Doctor Assigned:"));
        txtDoctorAssigned = new JTextField();
        panel.add(txtDoctorAssigned);

        // Ward Inputs
        panel.add(new JLabel("Ward Number:"));
        txtWardNumber = new JTextField();
        panel.add(txtWardNumber);

        panel.add(new JLabel("Ward Capacity:"));
        txtCapacity = new JTextField();
        panel.add(txtCapacity);

        // Bill Inputs
        panel.add(new JLabel("Total Bill Cost:"));
        txtTotalCost = new JTextField();
        panel.add(txtTotalCost);

        panel.add(new JLabel("Services Provided:"));
        txtServicesProvided = new JTextField();
        panel.add(txtServicesProvided);

        // Buttons
        JButton btnAddPatient = new JButton("Add Patient");
        btnAddPatient.addActionListener(e -> addPatient());
        panel.add(btnAddPatient);

        JButton btnAddAppointment = new JButton("Add Appointment");
        btnAddAppointment.addActionListener(e -> addAppointment());
        panel.add(btnAddAppointment);

        JButton btnAssignWard = new JButton("Assign Ward");
        btnAssignWard.addActionListener(e -> assignWard());
        panel.add(btnAssignWard);

        JButton btnGenerateBill = new JButton("Generate Bill");
        btnGenerateBill.addActionListener(e -> generateBill());
        panel.add(btnGenerateBill);

        JButton btnDisplayPatients = new JButton("Display Patients");
        btnDisplayPatients.addActionListener(e -> displayPatients());
        panel.add(btnDisplayPatients);

        JButton btnDisplayAppointments = new JButton("Display Appointments");
        btnDisplayAppointments.addActionListener(e -> displayAppointments());
        panel.add(btnDisplayAppointments);

        JButton btnDisplayWards = new JButton("Display Wards");
        btnDisplayWards.addActionListener(e -> displayWards());
        panel.add(btnDisplayWards);

        JButton btnDisplayBills = new JButton("Display Bills");
        btnDisplayBills.addActionListener(e -> displayBills());
        panel.add(btnDisplayBills);

        frame.setSize(500, 600);
        frame.setVisible(true);

        // Initialize JDBC connection
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to the database: " + e.getMessage());
        }
    }

    // Insert Patient into Database
    public void addPatient() {
        try {
            int patientID = Integer.parseInt(txtPatientID.getText());
            if (patientID <= 0) {
                JOptionPane.showMessageDialog(frame, "Patient ID must be greater than 0.");
                return;
            }

            String name = txtName.getText();
            int age = Integer.parseInt(txtAge.getText());
            String disease = txtDisease.getText();

            Patient newPatient = new Patient(patientID, name, age, disease);
            patients.add(newPatient);

            // Insert into Database
            String query = "INSERT INTO patients (patientID, name, age, disease) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, patientID);
                pst.setString(2, name);
                pst.setInt(3, age);
                pst.setString(4, disease);
                pst.executeUpdate();
                textArea.append("Patient added: " + name + "\n");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for Patient ID, Age.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding patient: " + e.getMessage());
        }
    }

    // Add Appointment to Database
    public void addAppointment() {
        try {
            int appointmentID = Integer.parseInt(txtAppointmentID.getText());
            if (appointmentID <= 0) {
                JOptionPane.showMessageDialog(frame, "Appointment ID must be greater than 0.");
                return;
            }

            int patientID = Integer.parseInt(txtPatientID.getText());
            Date appointmentDate = new Date();
            String doctorAssigned = txtDoctorAssigned.getText();

            Appointment newAppointment = new Appointment(appointmentID, patientID, appointmentDate, doctorAssigned);
            appointments.add(newAppointment);

            // Insert Appointment into Database
            String query = "INSERT INTO appointments (appointmentID, patientID, appointmentDate, doctorAssigned) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, appointmentID);
                pst.setInt(2, patientID);
                pst.setTimestamp(3, new java.sql.Timestamp(appointmentDate.getTime()));
                pst.setString(4, doctorAssigned);
                pst.executeUpdate();
                textArea.append("Appointment added for Patient ID: " + patientID + "\n");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding appointment: " + e.getMessage());
        }
    }

    // Assign Ward to a Patient
    public void assignWard() {
        try {
            int wardNumber = Integer.parseInt(txtWardNumber.getText());
            int patientID = Integer.parseInt(txtPatientID.getText());

            // Find the ward by ward number
            Ward ward = null;
            for (Ward w : wards) {
                if (w.wardNumber == wardNumber) {
                    ward = w;
                    break;
                }
            }

            // Find the patient by patient ID
            Patient patient = null;
            for (Patient p : patients) {
                if (p.patientID == patientID) {
                    patient = p;
                    break;
                }
            }

            if (ward != null && patient != null && ward.assignPatient(patient)) {
                textArea.append("Patient " + patient.name + " assigned to Ward " + wardNumber + "\n");
            } else {
                textArea.append("Could not assign patient to ward.\n");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for Ward Number and Patient ID.");
        }
    }

    // Generate Bill for a Patient
    public void generateBill() {
        try {
            int patientID = Integer.parseInt(txtPatientID.getText());
            double totalCost = Double.parseDouble(txtTotalCost.getText());
            String servicesProvided = txtServicesProvided.getText();

            Bill bill = new Bill(patientID, totalCost, servicesProvided);
            bills.add(bill);

            // Insert Bill into Database
            String query = "INSERT INTO bills (patientID, totalCost, servicesProvided) VALUES (?, ?, ?)";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, patientID);
                pst.setDouble(2, totalCost);
                pst.setString(3, servicesProvided);
                pst.executeUpdate();
                textArea.append("Bill generated for Patient ID: " + patientID + "\n");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for Patient ID and Bill Cost.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error generating bill: " + e.getMessage());
        }
    }

    // Display Patients in the system
    public void displayPatients() {
        textArea.setText("Patients List:\n");
        for (Patient patient : patients) {
            patient.displayPatientDetails();
            textArea.append("\n");
        }
    }

    // Display Appointments from the database
    public void displayAppointments() {
        textArea.setText("Appointments List:\n");
        try {
            String query = "SELECT * FROM appointments";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int appointmentID = rs.getInt("appointmentID");
                int patientID = rs.getInt("patientID");
                Date appointmentDate = rs.getTimestamp("appointmentDate");
                String doctorAssigned = rs.getString("doctorAssigned");

                textArea.append("Appointment ID: " + appointmentID + ", Patient ID: " + patientID + ", Date: " + appointmentDate + ", Doctor: " + doctorAssigned + "\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error displaying appointments: " + e.getMessage());
        }
    }

    // Display Wards in the system
    public void displayWards() {
        textArea.setText("Wards List:\n");
        for (Ward ward : wards) {
            ward.displayWardDetails();
            textArea.append("\n");
        }
    }

    // Display Bills from the database
    public void displayBills() {
        textArea.setText("Bills List:\n");
        try {
            String query = "SELECT * FROM bills";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int patientID = rs.getInt("patientID");
                double totalCost = rs.getDouble("totalCost");
                String servicesProvided = rs.getString("servicesProvided");

                textArea.append("Patient ID: " + patientID + ", Total Cost: " + totalCost + ", Services: " + servicesProvided + "\n");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error displaying bills: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new V418050();
    }
}
