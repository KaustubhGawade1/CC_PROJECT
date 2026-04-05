import React, { useState } from 'react'
import './App.css'
import Assets from './components/products/Assets'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Home from './components/home/Home'
import Navbar from './components/shared/Navbar'
import About from './components/About'
import Contact from './components/Contact'
import { Toaster } from 'react-hot-toast'
import LogIn from './components/auth/LogIn'
import PrivateRoute from './components/PrivateRoute'
import Register from './components/auth/Register'

import AddAsset from "./pages/AddAsset";

function App() {
  return (
    <React.Fragment>
      <Router>
        <Navbar />
        <Routes>
          <Route path='/' element={ <Home />}/>
          <Route path='/products' element={ <Assets />}/>
          <Route path='/about' element={ <About />}/>
          <Route path='/contact' element={ <Contact />}/>
      
        <Route path="/add-asset" element={<AddAsset />} />
          <Route path='/' element={<PrivateRoute />}>
            
          </Route>

        <Route path='/' element={<PrivateRoute publicPage />}>
            <Route path='/login' element={ <LogIn />}/>
            <Route path='/register' element={ <Register />}/>
          </Route>
        </Routes>
      </Router>
      <Toaster position='bottom-center'/>
    </React.Fragment>
  )
}

export default App
