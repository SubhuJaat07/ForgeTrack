import * as Print from 'expo-print';
import { Job } from '@/types';
import { formatCurrency, formatDuration } from './theme';

export async function generateJobReport(job: Job): Promise<void> {
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>ForgeTrack Job Report</title>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Helvetica', 'Arial', sans-serif; padding: 40px; color: #1e293b; }
        .header { background: linear-gradient(135deg, #6C5CE7, #a29bfe); color: white; padding: 30px; border-radius: 12px; margin-bottom: 30px; }
        .header h1 { font-size: 24px; margin-bottom: 8px; }
        .header p { opacity: 0.9; font-size: 14px; }
        .section { margin-bottom: 24px; }
        .section h2 { font-size: 18px; color: #6C5CE7; border-bottom: 2px solid #6C5CE7; padding-bottom: 8px; margin-bottom: 16px; }
        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .info-item { background: #f8fafc; padding: 12px; border-radius: 8px; }
        .info-item label { font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
        .info-item p { font-size: 16px; font-weight: 600; margin-top: 4px; }
        .status-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .status-completed { background: #d4edda; color: #155724; }
        .photos-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .photo-item { border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
        .photo-item img { width: 100%; height: 150px; object-fit: cover; }
        .photo-label { padding: 8px; font-size: 12px; font-weight: 600; text-transform: uppercase; color: #64748b; }
        .notes-list { list-style: none; }
        .notes-list li { padding: 8px 0; border-bottom: 1px solid #f1f5f9; font-size: 14px; }
        .signature-section { text-align: center; margin-top: 30px; }
        .signature-section img { max-width: 300px; border: 1px dashed #ccc; padding: 10px; }
        .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #e2e8f0; text-align: center; font-size: 12px; color: #94a3b8; }
        .revenue-box { background: #d4edda; padding: 16px; border-radius: 8px; text-align: center; }
        .revenue-box .amount { font-size: 32px; font-weight: 700; color: #155724; }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>ForgeTrack Job Report</h1>
        <p>${job.title} - Report generated on ${new Date().toLocaleDateString()}</p>
      </div>

      <div class="section">
        <h2>Job Details</h2>
        <div class="info-grid">
          <div class="info-item">
            <label>Job Title</label>
            <p>${job.title}</p>
          </div>
          <div class="info-item">
            <label>Status</label>
            <p><span class="status-badge status-completed">${job.status.toUpperCase()}</span></p>
          </div>
          <div class="info-item">
            <label>Client</label>
            <p>${job.clientName}</p>
          </div>
          <div class="info-item">
            <label>Priority</label>
            <p>${job.priority.toUpperCase()}</p>
          </div>
          <div class="info-item">
            <label>Scheduled Date</label>
            <p>${new Date(job.scheduledDate).toLocaleDateString()}</p>
          </div>
          <div class="info-item">
            <label>Duration</label>
            <p>${job.totalDuration ? formatDuration(job.totalDuration) : 'N/A'}</p>
          </div>
        </div>
      </div>

      <div class="section">
        <h2>Location</h2>
        <div class="info-item">
          <label>Address</label>
          <p>${job.location.address || 'Not specified'}</p>
        </div>
      </div>

      ${job.description ? `
      <div class="section">
        <h2>Description</h2>
        <p style="color: #475569; line-height: 1.6;">${job.description}</p>
      </div>
      ` : ''}

      <div class="section">
        <h2>Financial Summary</h2>
        <div class="info-grid">
          <div class="revenue-box">
            <label>Revenue</label>
            <p class="amount">${formatCurrency(job.revenue)}</p>
          </div>
          <div class="info-item">
            <label>Cost</label>
            <p>${formatCurrency(job.cost)}</p>
          </div>
        </div>
      </div>

      ${job.photos.length > 0 ? `
      <div class="section">
        <h2>Photos (${job.photos.length})</h2>
        <div class="photos-grid">
          ${job.photos.map((photo) => `
            <div class="photo-item">
              <div class="photo-label">${photo.type} - ${photo.annotation || 'No annotation'}</div>
            </div>
          `).join('')}
        </div>
      </div>
      ` : ''}

      ${job.notes.length > 0 ? `
      <div class="section">
        <h2>Notes</h2>
        <ul class="notes-list">
          ${job.notes.map((note) => `<li>${note}</li>`).join('')}
        </ul>
      </div>
      ` : ''}

      <div class="footer">
        <p>Generated by ForgeTrack - Professional Field Service Tracker</p>
        <p>${new Date().toLocaleString()}</p>
      </div>
    </body>
    </html>
  `;

  try {
    await Print.printAsync({
      html,
    });
  } catch (error) {
    console.error('PDF generation failed:', error);
    throw error;
  }
}
