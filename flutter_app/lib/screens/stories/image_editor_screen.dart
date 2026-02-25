import 'dart:io';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:path_provider/path_provider.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:alumni_portal/config/theme.dart';

class ImageEditorScreen extends StatefulWidget {
  final File imageFile;

  const ImageEditorScreen({super.key, required this.imageFile});

  @override
  State<ImageEditorScreen> createState() => _ImageEditorScreenState();
}

class _ImageEditorScreenState extends State<ImageEditorScreen> {
  final GlobalKey _imageKey = GlobalKey();
  final List<TextOverlay> _textOverlays = [];
  final List<DrawingPoint> _drawingPoints = [];
  
  late File _currentImage;
  bool _isDrawingMode = false;
  bool _isTextMode = false;
  Color _selectedColor = Colors.white;
  Color _drawingColor = Colors.white;
  double _strokeWidth = 5.0;
  
  final List<Color> _colors = [
    Colors.white,
    Colors.black,
    Colors.red,
    Colors.blue,
    Colors.green,
    Colors.yellow,
    Colors.purple,
    Colors.orange,
  ];

  @override
  void initState() {
    super.initState();
    _currentImage = widget.imageFile;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        leading: IconButton(
          icon: const Icon(Icons.close, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text('Edit', style: TextStyle(color: Colors.white)),
        actions: [
          IconButton(
            icon: const Icon(Icons.undo, color: Colors.white),
            onPressed: _undo,
          ),
          TextButton(
            onPressed: _saveAndReturn,
            child: const Text(
              'Done',
              style: TextStyle(
                color: AppColors.primary,
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: RepaintBoundary(
              key: _imageKey,
              child: Stack(
                fit: StackFit.expand,
                children: [
                  // Original image
                  Image.file(
                    _currentImage,
                    fit: BoxFit.contain,
                  ),
                  // Drawing layer
                  if (_drawingPoints.isNotEmpty)
                    CustomPaint(
                      painter: DrawingPainter(_drawingPoints),
                    ),
                  // Text overlays
                  ..._textOverlays.map((overlay) => _buildTextOverlay(overlay)),
                  // Drawing gesture detector
                  if (_isDrawingMode)
                    GestureDetector(
                      onPanStart: (details) {
                        setState(() {
                          _drawingPoints.add(
                            DrawingPoint(
                              offset: details.localPosition,
                              paint: Paint()
                                ..color = _drawingColor
                                ..strokeWidth = _strokeWidth
                                ..strokeCap = StrokeCap.round,
                            ),
                          );
                        });
                      },
                      onPanUpdate: (details) {
                        setState(() {
                          _drawingPoints.add(
                            DrawingPoint(
                              offset: details.localPosition,
                              paint: Paint()
                                ..color = _drawingColor
                                ..strokeWidth = _strokeWidth
                                ..strokeCap = StrokeCap.round,
                            ),
                          );
                        });
                      },
                      onPanEnd: (details) {
                        setState(() {
                          _drawingPoints.add(DrawingPoint(offset: null, paint: null));
                        });
                      },
                    ),
                ],
              ),
            ),
          ),
          // Bottom toolbar
          _buildBottomToolbar(),
        ],
      ),
    );
  }

  Widget _buildTextOverlay(TextOverlay overlay) {
    return Positioned(
      left: overlay.position.dx,
      top: overlay.position.dy,
      child: GestureDetector(
        onPanUpdate: (details) {
          setState(() {
            overlay.position = Offset(
              overlay.position.dx + details.delta.dx,
              overlay.position.dy + details.delta.dy,
            );
          });
        },
        onTap: () => _editText(overlay),
        onLongPress: () => _deleteText(overlay),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          decoration: BoxDecoration(
            color: overlay.backgroundColor,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Text(
            overlay.text,
            style: TextStyle(
              color: overlay.textColor,
              fontSize: overlay.fontSize,
              fontWeight: FontWeight.bold,
              shadows: const [
                Shadow(
                  color: Colors.black45,
                  offset: Offset(1, 1),
                  blurRadius: 2,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildBottomToolbar() {
    return Container(
      padding: EdgeInsets.fromLTRB(
        16,
        16,
        16,
        MediaQuery.of(context).padding.bottom + 16,
      ),
      decoration: const BoxDecoration(
        color: Colors.black87,
        border: Border(top: BorderSide(color: Colors.white24)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Color picker (shown when drawing or text mode)
          if (_isDrawingMode || _isTextMode) ...[
            SizedBox(
              height: 40,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _colors.length,
                itemBuilder: (context, index) {
                  final color = _colors[index];
                  final isSelected = _isDrawingMode
                      ? color == _drawingColor
                      : color == _selectedColor;
                  return GestureDetector(
                    onTap: () {
                      setState(() {
                        if (_isDrawingMode) {
                          _drawingColor = color;
                        } else {
                          _selectedColor = color;
                        }
                      });
                    },
                    child: Container(
                      width: 40,
                      height: 40,
                      margin: const EdgeInsets.only(right: 8),
                      decoration: BoxDecoration(
                        color: color,
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: isSelected ? AppColors.primary : Colors.white24,
                          width: 3,
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 16),
          ],
          // Stroke width slider (for drawing mode)
          if (_isDrawingMode) ...[
            Row(
              children: [
                const Icon(Icons.brush, color: Colors.white, size: 16),
                Expanded(
                  child: Slider(
                    value: _strokeWidth,
                    min: 2,
                    max: 20,
                    activeColor: AppColors.primary,
                    onChanged: (value) {
                      setState(() => _strokeWidth = value);
                    },
                  ),
                ),
                const Icon(Icons.brush, color: Colors.white, size: 24),
              ],
            ),
            const SizedBox(height: 16),
          ],
          // Main tools
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildToolButton(
                icon: Icons.text_fields,
                label: 'Text',
                isActive: _isTextMode,
                onTap: () {
                  setState(() {
                    _isTextMode = !_isTextMode;
                    _isDrawingMode = false;
                  });
                  if (_isTextMode) _addText();
                },
              ),
              _buildToolButton(
                icon: Icons.brush,
                label: 'Draw',
                isActive: _isDrawingMode,
                onTap: () {
                  setState(() {
                    _isDrawingMode = !_isDrawingMode;
                    _isTextMode = false;
                  });
                },
              ),
              _buildToolButton(
                icon: Icons.crop,
                label: 'Crop',
                isActive: false,
                onTap: _cropImage,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildToolButton({
    required IconData icon,
    required String label,
    required bool isActive,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: isActive ? AppColors.primary : Colors.white24,
              shape: BoxShape.circle,
            ),
            child: Icon(
              icon,
              color: Colors.white,
              size: 24,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              color: isActive ? AppColors.primary : Colors.white,
              fontSize: 12,
              fontWeight: isActive ? FontWeight.bold : FontWeight.normal,
            ),
          ),
        ],
      ),
    );
  }

  void _addText() {
    final controller = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Add Text'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Type something...',
            border: OutlineInputBorder(),
          ),
          maxLines: null,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty) {
                setState(() {
                  _textOverlays.add(
                    TextOverlay(
                      text: controller.text.trim(),
                      position: const Offset(50, 100),
                      textColor: _selectedColor,
                      backgroundColor: Colors.transparent,
                      fontSize: 24,
                    ),
                  );
                  _isTextMode = false;
                });
              }
              Navigator.pop(context);
            },
            child: const Text('Add'),
          ),
        ],
      ),
    );
  }

  void _editText(TextOverlay overlay) {
    final controller = TextEditingController(text: overlay.text);
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Edit Text'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Type something...',
            border: OutlineInputBorder(),
          ),
          maxLines: null,
        ),
        actions: [
          TextButton(
            onPressed: () {
              setState(() => _textOverlays.remove(overlay));
              Navigator.pop(context);
            },
            child: const Text('Delete', style: TextStyle(color: Colors.red)),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              if (controller.text.trim().isNotEmpty) {
                setState(() {
                  overlay.text = controller.text.trim();
                });
              }
              Navigator.pop(context);
            },
            child: const Text('Save'),
          ),
        ],
      ),
    );
  }

  void _deleteText(TextOverlay overlay) {
    setState(() => _textOverlays.remove(overlay));
  }

  Future<void> _cropImage() async {
    try {
      final croppedFile = await ImageCropper().cropImage(
        sourcePath: _currentImage.path,
        compressQuality: 100,
        uiSettings: [
          AndroidUiSettings(
            toolbarTitle: 'Crop Image',
            toolbarColor: AppColors.primary,
            toolbarWidgetColor: Colors.white,
            activeControlsWidgetColor: AppColors.primary,
            initAspectRatio: CropAspectRatioPreset.original,
            lockAspectRatio: false,
          ),
          IOSUiSettings(
            title: 'Crop Image',
          ),
        ],
      );

      if (croppedFile != null) {
        setState(() {
          _currentImage = File(croppedFile.path);
          // Clear drawings and text overlays after crop
          _drawingPoints.clear();
          _textOverlays.clear();
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error cropping image: $e')),
        );
      }
    }
  }

  void _undo() {
    setState(() {
      if (_drawingPoints.isNotEmpty) {
        // Remove last stroke
        while (_drawingPoints.isNotEmpty && _drawingPoints.last.offset != null) {
          _drawingPoints.removeLast();
        }
        if (_drawingPoints.isNotEmpty) {
          _drawingPoints.removeLast(); // Remove the null separator
        }
      } else if (_textOverlays.isNotEmpty) {
        _textOverlays.removeLast();
      }
    });
  }

  Future<void> _saveAndReturn() async {
    try {
      // Capture the edited image
      final boundary = _imageKey.currentContext!.findRenderObject() as RenderRepaintBoundary;
      final image = await boundary.toImage(pixelRatio: 2.0); // Reduced from 3.0 for better performance
      final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
      final bytes = byteData!.buffer.asUint8List();

      // Save to temporary file
      final tempDir = await getTemporaryDirectory();
      final file = File('${tempDir.path}/edited_story_${DateTime.now().millisecondsSinceEpoch}.png');
      await file.writeAsBytes(bytes);

      if (mounted) {
        Navigator.pop(context, file);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error saving image: $e')),
        );
      }
    }
  }
}

class TextOverlay {
  String text;
  Offset position;
  Color textColor;
  Color backgroundColor;
  double fontSize;

  TextOverlay({
    required this.text,
    required this.position,
    required this.textColor,
    required this.backgroundColor,
    required this.fontSize,
  });
}

class DrawingPoint {
  final Offset? offset;
  final Paint? paint;

  DrawingPoint({this.offset, this.paint});
}

class DrawingPainter extends CustomPainter {
  final List<DrawingPoint> points;

  DrawingPainter(this.points);

  @override
  void paint(Canvas canvas, Size size) {
    for (int i = 0; i < points.length - 1; i++) {
      if (points[i].offset != null && points[i + 1].offset != null) {
        canvas.drawLine(
          points[i].offset!,
          points[i + 1].offset!,
          points[i].paint!,
        );
      }
    }
  }

  @override
  bool shouldRepaint(DrawingPainter oldDelegate) => true;
}
