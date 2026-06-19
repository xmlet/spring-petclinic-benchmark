const path = require('path');
const webpack = require('webpack');

module.exports = {
  devtool: 'source-map',
  entry: ['./src/main.tsx'],
  output: {
    path: path.join(__dirname, 'public/dist/'),
    filename: 'bundle.js',
    publicPath: '/dist/'
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production'),
      },
      __API_SERVER_URL__: JSON.stringify('http://localhost:8082')
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: true
    }),
    new webpack.optimize.UglifyJsPlugin({
      sourceMap: true
    })
  ],
  resolve: {
    extensions: ['.ts', '.tsx', '.js']
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.less$/,
        use: ['style-loader', 'css-loader', 'less-loader'],
        include: path.join(__dirname, 'src/styles')
      },
      {
        test: /\.(png|jpg)$/,
        loader: 'url-loader',
        options: { limit: 25000 }
      },
      {
        test: /\.(eot|svg|ttf|woff|woff2)$/,
        loader: 'file-loader',
        options: { name: 'public/fonts/[name].[ext]' }
      },
      {
        test: /\.tsx?$/,
        use: ['babel-loader', loader: 'ts-loader']
        include: path.join(__dirname, 'src')
      }
    ]
  }
};
