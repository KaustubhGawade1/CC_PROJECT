import { FaEnvelope, FaMapMarkedAlt, FaPhone } from "react-icons/fa";

const Contact = () => {
    return (
        <div
            className="flex items-center justify-center min-h-screen py-12 bg-cover bg-center"
            style={{ backgroundImage: "url('')" }}
        >
            <div className="bg-white shadow-lg rounded-lg p-8 w-full max-w-lg text-center">
                <h1 className="text-4xl font-bold mb-6">Contact Us</h1>
                <p className="text-gray-600 mb-4">
                    I would love to hear from you! Please reach out using the details below.
                </p>

                <div className="mt-8">
                    <h2 className="text-lg font-semibold mb-4">Contact Information</h2>
                    <div className="flex flex-col items-center space-y-4">
                        <div className="flex items-center justify-center">
                            <FaPhone className="text-blue-500 mr-2" />
                            <span className="text-gray-600">+91 7820832647</span>
                        </div>

                        <div className="flex items-center justify-center">
                            <FaEnvelope className="text-blue-500 mr-2" />
                            <span className="text-gray-600">gawadekaustubh67@gmail.com</span>
                        </div>

                        <div className="flex items-center justify-center">
                            <FaMapMarkedAlt className="text-blue-500 mr-2" />
                            <span className="text-gray-600">Pimpri-Chinchwad, Pune</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Contact;
