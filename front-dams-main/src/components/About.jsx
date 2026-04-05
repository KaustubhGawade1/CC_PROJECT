import ProductCard from "./shared/ProductCard";
import bannerImagefour from "../assets/sliders/assets4.jpeg";

const About = () => {
    return (
        <div className="max-w-7xl mx-auto px-4 py-8">
            <h1 className="text-slate-800 text-4xl font-bold text-center mb-12">
                About Me
            </h1>

            <div className="flex flex-col lg:flex-row justify-center items-center mb-15">
                <div className="w-full md:w-1/2 text-center md:text-left">
                    <p className="text-lg mb-4">
                        Hello! I am Kaustubh Gawade, a passionate and dedicated Full Stack Developer with strong expertise in Java Spring Boot, React.js, and PostgreSQL. I enjoy building scalable, secure, and user-friendly applications that solve real-world problems efficiently.
                    </p>

                    <p className="text-lg mb-4">
                        This project is a Digital Asset Management System designed to help organizations store, manage, and access their digital assets seamlessly. It features user authentication with JWT security, role-based access control, and a responsive React.js frontend.
                    </p>

                    <p className="text-lg mb-4">
                        My skills include:
                        <ul className="list-disc list-inside mt-2">
                            <li>Java Spring Boot REST APIs</li>
                            <li>React.js frontend development</li>
                            <li>PostgreSQL & MySQL database management</li>
                            <li>Docker containerization and deployment</li>
                            <li>Software architecture design</li>
                        </ul>
                    </p>

                    <p className="text-lg mb-4">
                        For deployment, I have containerized the backend using <span className="font-semibold">Docker</span> and deployed it on <span className="font-semibold">Render</span> for reliable hosting. The frontend is version-controlled on <span className="font-semibold">GitHub</span> and deployed seamlessly using <span className="font-semibold">Vercel</span> for fast, global delivery.
                    </p>

                    <p className="text-lg mb-4">
                        I am continuously learning and enhancing my skill set to deliver robust and production-grade solutions. Feel free to explore my projects and connect for collaboration or opportunities.
                    </p>
                </div>

                <div className="w-full md:w-1/2 mb-4 md:mb-0 flex justify-center">
                    <img
                        src={bannerImagefour}
                        alt="Kaustubh Gawade"
                        className="w-72 h-auto rounded-lg shadow-lg transform transition-transform duration-300 hover:scale-105"
                    />
                </div>
            </div>

            {/* Uncomment if you want to showcase assets/products */}
            {/* <div className="py-7 space-y-8">
                <h1 className="text-slate-800 text-4xl font-bold text-center">
                    Our Assets
                </h1>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {products.map((product, index) => (
                        <ProductCard 
                            key={index}
                            image={product.image}
                            filename={product.filename}
                            contentType={product.contentType}
                            specialsize={product.specialsize}
                            size={product.size}
                            about
                        />
                    ))}
                </div>
            </div> */}
        </div>
    );
}

export default About;
