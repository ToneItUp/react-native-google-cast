require 'json'
package = JSON.parse(File.read(File.join(__dir__, '../', 'package.json')))

Pod::Spec.new do |s|
  s.name          = package['name']
  s.version       = package['version']
  s.summary       = package['description']
  s.license       = package['license']

  s.authors       = package['author']
  s.homepage      = package['homepage']
  s.platform      = :ios, '9.0'

  s.source        = { :git => 'https://github.com/react-native-google-cast/react-native-google-cast.git', :tag => s.version.to_s }
  s.default_subspec = 'Default'

  s.dependency      'React'

  s.subspec 'Default' do |ss|
    ss.dependency "#{package['name']}/RNGoogleCast"
    ss.dependency 'google-cast-sdk', '<= 4.3.0'
  end

  s.subspec 'NoBluetooth' do |ss|
    ss.dependency "#{package['name']}/RNGoogleCast"
    ss.dependency 'google-cast-sdk-no-bluetooth', '4.5.0'
  end

  s.subspec 'Manual' do |ss|
    ss.dependency "#{package['name']}/RNGoogleCast"
  end

  s.subspec 'RNGoogleCast' do |ss|
    ss.source_files = 'RNGoogleCast/**/*.{h,m}'
    ss.dependency 'PromisesObjC'
  end
end
