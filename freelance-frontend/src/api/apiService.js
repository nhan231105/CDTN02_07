// API Configuration
export const API_BASE_URL = 'http://localhost:8082/api';

export const API_ENDPOINTS = {
  // Auth
  AUTH_REGISTER: '/auth/register',
  AUTH_LOGIN: '/auth/login',
  AUTH_HEALTH: '/auth/health',
  AUTH_ADMIN_STATS: '/auth/admin/stats',
  AUTH_SEARCH: '/auth/admin/search',

  // Jobs
  JOBS_LIST: '/jobs',
  JOBS_CREATE: '/jobs',
  JOBS_GET: (id) => `/jobs/${id}`,
  JOBS_UPDATE: (id) => `/jobs/${id}`,
  JOBS_STATUS: (id) => `/jobs/${id}/status`,
  JOBS_SEARCH: '/jobs/search',
  JOBS_EMPLOYER: (employerId) => `/jobs/employer/${employerId}`,

  // Applications
  APPLICATIONS_CREATE: (jobId) => `/jobs/${jobId}/applications`,
  APPLICATIONS_LIST: (jobId) => `/jobs/${jobId}/applications`,
  APPLICATIONS_DECISION: (appId) => `/applications/${appId}/decision`,

  // Reviews
  REVIEWS_CREATE: (jobId) => `/reviews/jobs/${jobId}`,
  REVIEWS_BY_JOB: (jobId) => `/reviews/jobs/${jobId}`,
  REVIEWS_BY_USER: (userId) => `/reviews/users/${userId}`,

  // Notifications
  NOTIFICATIONS_LIST: '/me/notifications',
  NOTIFICATIONS_READ: (id) => `/me/notifications/${id}/read`,
  NOTIFICATIONS_UNREAD: '/me/notifications/count/unread',

  // Profiles
  STUDENT_PROFILE_GET: (userId) => `/profiles/students/${userId}`,
  STUDENT_PROFILE_UPDATE: (userId) => `/profiles/students/${userId}`,
  EMPLOYER_PROFILE_GET: (userId) => `/profiles/employers/${userId}`,
  EMPLOYER_PROFILE_UPDATE: (userId) => `/profiles/employers/${userId}`,

  // Admin
  ADMIN_JOBS_MODERATION: (jobId) => `/admin/jobs/${jobId}/moderation`,
  ADMIN_USERS_STATUS: (userId) => `/admin/users/${userId}/status`,
  ADMIN_REPORTS_LIST: '/admin/reports',
  ADMIN_REPORTS_RESOLVE: (reportId) => `/admin/reports/${reportId}/resolve`,
};

// API Service class
class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async request(method, endpoint, data = null, params = null) {
    try {
      const url = new URL(this.baseURL + endpoint);
      
      // Add query parameters if provided
      if (params) {
        Object.keys(params).forEach(key => {
          url.searchParams.append(key, params[key]);
        });
      }

      const options = {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
      };

      // Add body for POST, PATCH, PUT requests
      if (data && (method === 'POST' || method === 'PATCH' || method === 'PUT')) {
        options.body = JSON.stringify(data);
      }

      const response = await fetch(url.toString(), options);
      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || `HTTP error! status: ${response.status}`);
      }

      return result;
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  // Auth endpoints
  register(userData) {
    return this.request('POST', API_ENDPOINTS.AUTH_REGISTER, userData);
  }

  login(credentials) {
    return this.request('POST', API_ENDPOINTS.AUTH_LOGIN, credentials);
  }

  getHealthStatus() {
    return this.request('GET', API_ENDPOINTS.AUTH_HEALTH);
  }

  getAdminStats() {
    return this.request('GET', API_ENDPOINTS.AUTH_ADMIN_STATS);
  }

  // Jobs endpoints
  getJobs(params = null) {
    return this.request('GET', API_ENDPOINTS.JOBS_LIST, null, params);
  }

  getJob(id) {
    return this.request('GET', API_ENDPOINTS.JOBS_GET(id));
  }

  createJob(jobData) {
    return this.request('POST', API_ENDPOINTS.JOBS_CREATE, jobData);
  }

  updateJob(id, employerId, jobData) {
    return this.request('PUT', API_ENDPOINTS.JOBS_UPDATE(id), jobData, { employerId });
  }

  updateJobStatus(id, employerId, status) {
    return this.request('PATCH', API_ENDPOINTS.JOBS_STATUS(id), { status }, { employerId });
  }

  searchJobs(params) {
    return this.request('GET', API_ENDPOINTS.JOBS_SEARCH, null, params);
  }

  getEmployerJobs(employerId) {
    return this.request('GET', API_ENDPOINTS.JOBS_EMPLOYER(employerId));
  }

  // Applications endpoints
  applyForJob(jobId, applicationData) {
    return this.request('POST', API_ENDPOINTS.APPLICATIONS_CREATE(jobId), applicationData);
  }

  getJobApplications(jobId, employerId) {
    return this.request('GET', API_ENDPOINTS.APPLICATIONS_LIST(jobId), null, { employerId });
  }

  approveApplication(appId, employerId) {
    return this.request('PATCH', API_ENDPOINTS.APPLICATIONS_DECISION(appId), { decision: 'APPROVED' }, { employerId });
  }

  rejectApplication(appId, employerId) {
    return this.request('PATCH', API_ENDPOINTS.APPLICATIONS_DECISION(appId), { decision: 'REJECTED' }, { employerId });
  }

  // Reviews endpoints
  createReview(jobId, reviewerId, revieweeId, reviewData) {
    return this.request('POST', API_ENDPOINTS.REVIEWS_CREATE(jobId), reviewData, { reviewerId, revieweeId });
  }

  getReviewsByJob(jobId) {
    return this.request('GET', API_ENDPOINTS.REVIEWS_BY_JOB(jobId));
  }

  getReviewsByUser(userId) {
    return this.request('GET', API_ENDPOINTS.REVIEWS_BY_USER(userId));
  }

  // Notifications endpoints
  getNotifications(userId, params = null) {
    const allParams = { userId, ...(params || {}) };
    return this.request('GET', API_ENDPOINTS.NOTIFICATIONS_LIST, null, allParams);
  }

  markNotificationAsRead(notificationId, userId) {
    return this.request('PATCH', API_ENDPOINTS.NOTIFICATIONS_READ(notificationId), null, { userId });
  }

  getUnreadCount(userId) {
    return this.request('GET', API_ENDPOINTS.NOTIFICATIONS_UNREAD, null, { userId });
  }

  // Profiles endpoints
  getStudentProfile(userId) {
    return this.request('GET', API_ENDPOINTS.STUDENT_PROFILE_GET(userId));
  }

  updateStudentProfile(userId, profileData) {
    return this.request('PATCH', API_ENDPOINTS.STUDENT_PROFILE_UPDATE(userId), profileData);
  }

  getEmployerProfile(userId) {
    return this.request('GET', API_ENDPOINTS.EMPLOYER_PROFILE_GET(userId));
  }

  updateEmployerProfile(userId, profileData) {
    return this.request('PATCH', API_ENDPOINTS.EMPLOYER_PROFILE_UPDATE(userId), profileData);
  }
}

export default new ApiService();
